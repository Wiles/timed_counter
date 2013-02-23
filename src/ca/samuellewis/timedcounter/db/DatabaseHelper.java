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

	static final String DB_NAME = "timedCounterDB";

	static final String SESSION_TABLE = "session";
	static final String SESSION_ID = "id";
	static final String SESSION_DATE = "date";
	static final String SESSION_DURATION = "duration";
	static final String SESSION_FINISHED = "FINISHED";

	static final String ENTRY_TABLE = "entry";
	static final String ENTRY_ID = "id";
	static final String ENTRY_SESSION = "sessionId";
	static final String ENTRY_VALUE = "value";

	static final String ID_IS_VALUE = "%s = %d";

	static final int DATABASE_VERSION = 1;

	private static final DateTimeFormatter DTF = ISODateTimeFormat
			.basicDateTime();

	public DatabaseHelper(final Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		String query = String.format("CREATE TABLE %s ( "
				+ "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "%s TEXT NOT NULL, " + "%s INTEGER NOT NULL, %s INTEGER);",
				SESSION_TABLE, SESSION_ID, SESSION_DATE, SESSION_DURATION,
				SESSION_FINISHED);
		db.execSQL(query);
		query = String
				.format("CREATE TABLE %s ("
						+ "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "%s INTEGER NOT NULL, "
						+ "sessionId INTEGER NOT NULL, "
						+ "FOREIGN KEY (%s) REFERENCES  %s (%s));",
						ENTRY_TABLE, ENTRY_ID, ENTRY_VALUE, ENTRY_SESSION,
						SESSION_TABLE, SESSION_ID);
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + ENTRY_TABLE);
		onCreate(db);
	}

	public Session getSession(final long id) {
		SQLiteDatabase db = null;
		try {

			db = getReadableDatabase();
			final Cursor results = db.query(SESSION_TABLE, new String[] {
					SESSION_DATE, SESSION_DURATION },
					String.format(ID_IS_VALUE, SESSION_ID, id), null, null,
					null, null, "1");

			final int dateIndex = results.getColumnIndex(SESSION_DATE);
			final int durationIndex = results.getColumnIndex(SESSION_DURATION);

			if (results.moveToFirst()) {

				final DateTime date = DTF.parseDateTime(results
						.getString(dateIndex));
				final long duration = results.getInt(durationIndex);
				return createSession(id, date, duration);
			}

			return null;
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void deleteSession(final long id) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			db.beginTransaction();
			db.delete(SESSION_TABLE,
					String.format(ID_IS_VALUE, SESSION_ID, id), null);
			db.delete(ENTRY_TABLE,
					String.format(ID_IS_VALUE, ENTRY_SESSION, id), null);
			db.setTransactionSuccessful();
			db.endTransaction();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void removeUnfinished() {

		SQLiteDatabase db = null;
		try {
			db = getReadableDatabase();
			final Cursor results = db.query(SESSION_TABLE,
					new String[] { SESSION_FINISHED },
					String.format("%s = 0", SESSION_FINISHED), null, null,
					null, String.format("%s desc", SESSION_ID));
			final int finishedIndex = results.getColumnIndex(SESSION_FINISHED);
			while (results.moveToNext()) {
				deleteSession(results.getLong(finishedIndex));
			}
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void finishSession(final long id) {

		SQLiteDatabase db = null;
		try {
			db = getReadableDatabase();
			final ContentValues cv = new ContentValues();
			cv.put(SESSION_FINISHED, true);
			db.update(SESSION_TABLE, cv,
					String.format(ID_IS_VALUE, SESSION_ID, id), null);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public Session[] getSessions() {

		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();

			final Cursor results = db.query(SESSION_TABLE, new String[] {
					SESSION_ID, SESSION_DATE, SESSION_DURATION },
					String.format("%s = 1", SESSION_FINISHED), null, null,
					null, String.format("%s desc", SESSION_ID));

			final Session[] ids = new Session[results.getCount()];

			final int idIndex = results.getColumnIndex(SESSION_ID);
			final int dateIndex = results.getColumnIndex(SESSION_DATE);
			final int durationIndex = results.getColumnIndex(SESSION_DURATION);

			int i = 0;

			while (results.moveToNext()) {

				final Long id = results.getLong(idIndex);
				final DateTime date = DTF.parseDateTime(results
						.getString(dateIndex));
				final long duration = results.getInt(durationIndex);
				ids[i] = createSession(id, date, duration);
				++i;
			}

			return ids;
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public long[] getValues(final long id) {

		SQLiteDatabase valueDb = null;
		try {
			valueDb = getReadableDatabase();
			final Cursor valueResults = valueDb.query(ENTRY_TABLE,
					new String[] { ENTRY_VALUE },
					String.format(ID_IS_VALUE, ENTRY_SESSION, id), null, null,
					null, ENTRY_VALUE);
			final long[] values = new long[valueResults.getCount()];

			final int valueIndex = valueResults.getColumnIndex(ENTRY_VALUE);

			int j = 0;
			while (valueResults.moveToNext()) {
				values[j] = valueResults.getLong(valueIndex);
				++j;
			}
			return values;
		} finally {
			if (valueDb != null) {
				valueDb.close();
			}
		}
	}

	public long getCount(final long id) {
		SQLiteDatabase countDb = null;
		try {
			countDb = getReadableDatabase();
			final Cursor count = countDb.rawQuery(String.format(
					"select count(*) as rows from %s where %s = %d",
					ENTRY_TABLE, ENTRY_SESSION, id), null);
			if (count.moveToFirst()) {
				return count.getInt(count.getColumnIndex("rows"));
			} else {
				return 0;
			}
		} finally {
			if (countDb != null) {
				countDb.close();
			}
		}

	}

	private Session createSession(final Long id, final DateTime date,
			final Long duration) {
		final Session session = new Session(date, duration, new ValuesSource() {
			@Override
			public long[] getValues() {
				return DatabaseHelper.this.getValues(id);
			}

			@Override
			public long getCount() {
				return DatabaseHelper.this.getCount(id);
			}
		});
		session.setId(id);
		return session;
	}

	public Session createSession(final long duration) {
		SQLiteDatabase db = null;
		try {
			final DateTime now = new DateTime();
			db = this.getWritableDatabase();
			final ContentValues sessionContent = new ContentValues();
			sessionContent.put(SESSION_DATE, DTF.print(now));
			sessionContent.put(SESSION_DURATION, duration);
			sessionContent.put(SESSION_FINISHED, false);
			final Long rowId = db.insert(SESSION_TABLE, null, sessionContent);

			return createSession(rowId, now, duration);

		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void addEntry(final Session session, final long value) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			final ContentValues sessionValue = new ContentValues();
			sessionValue.put(ENTRY_SESSION, session.getId());
			sessionValue.put(ENTRY_VALUE, value);
			db.insert(ENTRY_TABLE, null, sessionValue);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
}
