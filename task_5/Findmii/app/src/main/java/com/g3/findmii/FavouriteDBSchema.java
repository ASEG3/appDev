package com.g3.findmii;

import android.provider.BaseColumns;

/**
 * Created by tim on 25/11/15.
 */
public class FavouriteDBSchema {
            // To prevent someone from accidentally instantiating the contract class,
        // give it an empty constructor.
        public FavouriteDBSchema() {}

        /* Inner class that defines the table contents */
        public static abstract class FavouriteSchema implements BaseColumns {
            public static final String TABLE_NAME = "favourite";
            public static final String COLUMN_NAME_ID ="id";
            public static final String COLUMN_NAME_LATITUDE = "latitude";
            public static final String COLUMN_NAME_LONGITUDE = "longitude";
            public static final String COLUMN_NAME_POSTCODE = "postcode";
            public static final String COLUMN_NAME_ADDRESS = "address";
            public static final String COLUMN_NAME_AVG_PRICE = "averageprice";
            public static final String COLUMN_NAME_CREATED_AT = "add_date";
        }

}
