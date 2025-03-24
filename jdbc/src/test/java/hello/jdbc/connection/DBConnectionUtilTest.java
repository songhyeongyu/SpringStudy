package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class DBConnectionUtilTest {
    @Test
    void connection() throws SQLException {
        Connection connection = DBConnectionUtil.getConnection();
        assertThat(connection).isNotNull();
    }


}