/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.util;

import java.util.Calendar;
import java.util.Date;

public class DateCompare {

  public static int compareByDay(long date1, long date2) {
    return compareByDay(new Date(date1), new Date(date2));
  }

  public static int compareByDay(Date date1, Date date2) {

    // Create Calendar instances
    Calendar calendar1 = Calendar.getInstance();
    Calendar calendar2 = Calendar.getInstance();

    // Set the Date objects to the Calendar instances
    calendar1.setTime(date1);
    calendar2.setTime(date2);

    // Clear the time fields for each calendar
    clearTimeFields(calendar1);
    clearTimeFields(calendar2);

    // Compare the calendar dates
    int result = calendar1.compareTo(calendar2);
    if (result < 0) {
      return -1;
    } else if (result > 0) {
      return 1;
    } else {
      return 0;
    }
  }

  private static void clearTimeFields(Calendar calendar) {
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }
}
