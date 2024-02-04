package com.marcpg.botpg2;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    public static String TOKEN;
    public static String MARCPG;

    public static String PEGOS_ID;
    public static String PEGOS_MODS_ONLY;
    public static String PEGOS_TICKET_CATEGORY;

    public static String HECTUS_ID;
    public static String HECTUS_MODS_ONLY;
    public static String HECTUS_TICKET_CATEGORY;

    public static void load() {
        Properties properties = new Properties();

        try (FileInputStream stream = new FileInputStream("pg.properties")) {
            properties.load(stream);
        } catch (IOException e) {
            System.err.println("Couldn't load BotPG configuration (pg.properties): " + e);
        }

        TOKEN = properties.getProperty("token");
        MARCPG = properties.getProperty("marcpg");
        PEGOS_ID = properties.getProperty("pegos-id");

        // PegOS related configuration
        PEGOS_ID = properties.getProperty("pegos.id");
        PEGOS_MODS_ONLY = properties.getProperty("pegos.mods-only");
        PEGOS_TICKET_CATEGORY = properties.getProperty("pegos.ticket-category");

        // Hectus related configuration
        HECTUS_ID = properties.getProperty("hectus.id");
        HECTUS_MODS_ONLY = properties.getProperty("hectus.mods-only");
        PSQL_PASSWD = properties.getProperty("psql.passwd");
    }
}
