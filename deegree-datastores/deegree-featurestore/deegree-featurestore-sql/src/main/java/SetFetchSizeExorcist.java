import static java.sql.Connection.TRANSACTION_READ_COMMITTED;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SetFetchSizeExorcist {

    public static void main( String[] args )
                            throws Exception {
        Class.forName( "org.postgresql.Driver" ).newInstance();
        String url = "jdbc:postgresql://10.19.1.162:5432/cswrapideye_ebrim";
        Connection conn = DriverManager.getConnection( url, "postgres", "postgres" );
        conn.setAutoCommit( false );
        String sql = "SELECT data FROM idxtb_extrinsicobject WHERE internalId IN (SELECT DISTINCT(X1.internalId) FROM idxtb_extrinsicobject AS X1 WHERE X1.ep_parentIdentifier = ? LIMIT 1000)";
        PreparedStatement stmt = conn.prepareStatement( sql );
        stmt.setFetchSize( 100 );
        stmt.setString( 1, "XXXXXXXXXXXX" );
        ResultSet rs = stmt.executeQuery();
        System.out.println( "JO2" );
        while ( rs.next() ) {
            System.out.println( rs.getInt( 1 ) );
        }
        System.out.println( "JO" );
    }
}
