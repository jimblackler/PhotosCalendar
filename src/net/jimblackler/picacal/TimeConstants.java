package net.jimblackler.picacal;

public class TimeConstants {
  public static final int MILLISECONDS_PER_SECOND = 1000;
  public static final int SECONDS_PER_MINUTE = 60;
  public static final int MINUTES_PER_HOUR = 60;
  public static final int HOURS_PER_HALF_DAY = 12;
  public static final int HALF_DAYS_PER_DAY = 2;
  public static final int HOURS_PER_DAY = HOURS_PER_HALF_DAY * HALF_DAYS_PER_DAY;
  public static final int DAYS_PER_WEEK = 7;
  public static final int MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;
  public static final int MILLISECONDS_PER_HOUR = MINUTES_PER_HOUR * MILLISECONDS_PER_MINUTE;
  public static final int MILLISECONDS_PER_HALF_DAY = HOURS_PER_HALF_DAY * MILLISECONDS_PER_HOUR;
  public static final int MILLISECONDS_PER_DAY = HALF_DAYS_PER_DAY * MILLISECONDS_PER_HALF_DAY;
  public static final int MILLISECONDS_PER_WEEK = DAYS_PER_WEEK * MILLISECONDS_PER_DAY;
}