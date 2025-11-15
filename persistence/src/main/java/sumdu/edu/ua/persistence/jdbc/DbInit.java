package sumdu.edu.ua.persistence.jdbc;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public final class DbInit {

    private DbInit() {}

    public static void init() {
        try (Connection c = Db.get();
            Statement st = c.createStatement()) {

            try (var in = DbInit.class.getClassLoader().getResourceAsStream("schema.sql")) {
                if (in == null) {
                    throw new IllegalStateException("schema.sql not found in resources");
                }
                String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                for (String cmd : sql.split(";")) {
                    if (!cmd.isBlank()) {
                        st.execute(cmd);
                    }
                }
            }

            // Check if there is data in the books table
            var rs = st.executeQuery("SELECT COUNT(*) FROM books");
            if (rs.next() && rs.getInt(1) == 0) {
                // Add initial data if the table is empty
                try (var in = DbInit.class.getClassLoader().getResourceAsStream("initial-data.sql")) {
                    if (in != null) {
                        String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        for (String cmd : sql.split(";")) {
                            if (!cmd.isBlank() && !cmd.trim().startsWith("--")) {
                                st.execute(cmd);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("DB schema init failed", e);
        }
    }
}

