package ca.samuellewis.timedcounter.db;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ca.samuellewis.timedcounter.results.Session;
import ca.samuellewis.timedcounter.results.ValuesSource;

public class DatabaseHelper extends SQLiteOpenHelper {

	static final String dbName = "timedCounterDB";

	static final String sessionTable = "session";
	static final String sessionId = "id";
	static final String sessionDate = "date";
	static final String sessionDuration = "duration";

	static final String entryTable = "entry";
	static final String entryId = "id";
	static final String entrySession = "sessionId";
	static final String entryValue = "value";

	final DateTimeFormatter dtf = ISODateTimeFormat.basicDateTime();

	public DatabaseHelper(final Context context) {
		super(context, dbName, null, 3);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		String query = String.format("CREATE TABLE %s ( "
				+ "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "%s TEXT NOT NULL, " + "%s INTEGER NOT NULL);", sessionTable,
				sessionId, sessionDate, sessionDuration);
		db.execSQL(query);
		query = String.format("CREATE TABLE %s ("
				+ "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "%s INTEGER NOT NULL, " + "sessionId INTEGER NOT NULL, "
				+ "FOREIGN KEY (%s) REFERENCES  %s (%s));", entryTable,
				entryId, entryValue, entrySession, sessionTable, sessionId);
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + sessionTable);
		db.execSQL("DROP TABLE IF EXISTS " + entryTable);
		onCreate(db);
	}

	public void saveSession(final Session session) {
		final SQLiteDatabase db = this.getWritableDatabase();
		final ContentValues sessionContent = new ContentValues();
		sessionContent.put(sessionDate, dtf.print(session.getDate()));
		sessionContent.put(sessionDuration, session.getDuration());
		final long rowId = db.insert(sessionTable, null, sessionContent);

		for (final long value : session.getValues()) {
			final ContentValues sessionValue = new ContentValues();
			sessionValue.put(entrySession, rowId);
			sessionValue.put(entryValue, value);
			db.insert(entryTable, null, sessionValue);
		}
	}

	public Session getSession(final int id) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor results = db.query(sessionTable, new String[] {
				sessionDate, sessionDuration },
				String.format("%s = %d", sessionId, id), null, null, null,
				null, "1");

		final int dateIndex = results.getColumnIndex(sessionDate);
		final int durationIndex = results.getColumnIndex(sessionDuration);

		if (results.moveToFirst()) {

			final DateTime date = dtf.parseDateTime(results
					.getString(dateIndex));
			final int duration = results.getInt(durationIndex);
			return createSession(id, date, duration, db);
		}

		return null;

	}

	public Session[] getSessions() {
		final SQLiteDatabase db = getReadableDatabase();

		final Cursor results = db.query(sessionTable, new String[] { sessionId,
				sessionDate, sessionDuration }, null, null, null, null,
				String.format("%s desc", sessionId));

		final Session[] ids = new Session[results.getCount()];

		final int idIndex = results.getColumnIndex(sessionId);
		final int dateIndex = results.getColumnIndex(sessionDate);
		final int durationIndex = results.getColumnIndex(sessionDuration);

		int i = 0;

		while (results.moveToNext()) {

			final Integer id = results.getInt(idIndex);
			final DateTime date = dtf.parseDateTime(results
					.getString(dateIndex));
			final int duration = results.getInt(durationIndex);
			ids[i] = createSession(id, date, duration, db);
			++i;
		}

		return ids;
	}

	private Session createSession(final Integer id, final DateTime date,
			final int duration, final SQLiteDatabase db) {
		final Session session = new Session(date, duration, new ValuesSource() {

			@Override
			public long[] getValues() {
				final Cursor valueResults = db.query(entryTable,
						new String[] { entryValue },
						String.format("%s = %d", entrySession, id), null, null,
						null, entryId);
				final long[] values = new long[valueResults.getCount()];

				final int valueIndex = valueResults.getColumnIndex(entryValue);

				int j = 0;
				while (valueResults.moveToNext()) {
					values[j] = valueResults.getLong(valueIndex);
					++j;
				}
				return values;
			}

			@Override
			public int getCount() {
				final Cursor count = db.rawQuery(String.format(
						"select count(*) as rows from %s where %s = %d",
						entryTable, entrySession, id), null);
				if (count.moveToFirst()) {
					return count.getInt(count.getColumnIndex("rows"));
				} else {
					return 0;
				}
			}
		});
		session.setId(id);
		return session;
	}
}
