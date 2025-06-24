
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteBuffer;
import java.io.ByteOrder;
import java.sql.*;
import java.util.UUID;

public class HCPPath {

    private static final int SHORT_TYPE = 2;
    private static final int WCHAR_TYPE = 2;
    private static final int INT_TYPE = 4;
    private static final int LONG_TYPE = 8;
    private static final int DOUBLE_TYPE = 8;
    private static final int ID_TYPE = 16;
    static String locationFN = "";

    public HCPPath() {}

    public static void main(String[] args) throws Exception {
        String user = "os";
        String pwd = "xxxxxxxxxxxxxxxxxxxx";
        String drivername = "oracle.jdbc.OracleDriver";
        Driver myDriver = new oracle.jdbc.driver.OracleDriver();
        DriverManager.registerDriver(myDriver);

        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//yourhost:port/servicename", user, pwd);
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT CONTENT_REFERRAL_BLOB FROM DOCVERSION WHERE object_id='6CE4F9052F08915A8E0C0764CAB9311'");
        byte[] blob = null;
        while (rs.next()) {
            blob = rs.getBytes(1);
            break;
        }

        System.out.println("contentReferral: " + blob);

        ByteArrayInputStream in = new ByteArrayInputStream(blob);
        int version = readInt(in);
        System.out.println("version: " + version);

        int checksum = readInt(in);
        System.out.println("checksum: " + checksum);

        short type = readShort(in);
        System.out.println("type: " + type);

        short flags = readShort(in);
        System.out.println("flags: " + flags);

        byte[] contentId = readId(in);
        System.out.println("ContentID: " + contentId);

        String deviceGUID = null;
        String clipId = null;
        int provider = (version & 0xFFFF0000) >> 16;
        System.out.println("provider: " + provider);

        int fcpType = 0;

        if (provider == 7) {
            byte[] deviceId = readId(in);
            System.out.println("deviceId: " + deviceId);

            int alignment = readInt(in);
            System.out.println("alignment: " + alignment);

            long retentionPeriod = readLong(in);
            System.out.println("retentionPeriod: " + retentionPeriod);

            int ceCount = readInt(in);
            System.out.println("ceCount: " + ceCount);

            alignment = readInt(in);
            System.out.println("alignment: " + alignment);

            short[] fileLocation = new short[8];
            for (int i = 0; i < 8; i++) {
                fileLocation[i] = readShort(in);
                System.out.println("fileLocation: " + i + " -- " + fileLocation[i]);
            }

            int[] contentElements = new int[ceCount];
            for (int i = 0; i < ceCount; i++) {
                contentElements[i] = readInt(in);
            }

            ByteBuffer bb = ByteBuffer.wrap(deviceId);
            UUID uuid = new UUID(bb.getLong(), bb.getLong());
            deviceGUID = ConvertUUIToGUID(uuid.toString());
            System.out.println("deviceGUID:"+ deviceGUID);    
                
            ByteBuffer bb1 = ByteBuffer.wrap(contentId);
            UUID contentuuid = new UUID(bb1.getLong(), bb1.getLong());
            contentGUID = ConvertUUIToGUID(contentuuid.toString());
            System.out.println("contentGUID:"+ contentGUID);  
            StringBuffer location = new StringBuffer(100);
            location.append("[");

            int first_depth=0;
            for (int i=0; i<8; i++)
                {
                    if(i=0) {
                      first_depth = fileLocation[i];
                    }
                     if(i<first_depth && i!=0) {
                         location.append(fileLocation[i])
                         locationFN+="/FN"+fileLocation[i];
                    }                   
                }
            location.append("]");
            System.out.println("locationFN_HCP:"+ locationFN); 
            fcpType = 7;
        }
    if (provider == 1) {
    try 
    {
        fcpType = 0;
        byte[] bytebuf = new byte[64];
        in.read(bytebuf); 
        bytebuf.order(ByteOrder.LITTLE_ENDIAN);
        clipId = bytebuf.asCharBuffer().toString().trim();
        fcpType = 1;
    }
    catch (IOException e) {
        System.out.println("Exception thrown trying to extract C-ClipID: " + 
            e.getLocalizedMessage());
    }
}

if (provider == 2) {
    try {
        fcpType = 0;
        int docid = readInt(in);
        int ceCount = readInt(in);
        
        fcpType = 2;
    }
    catch (Exception localException) {
    }
}

if (provider == 3) {
    fcpType = 0;
    
    byte[] storeId = readId(in);
    byte[] deviceId = readId(in);
    
    fcpType = 3;
}

public static short readShort(InputStream stream) {
    try {
        int b1 = stream.read();
        int b2 = stream.read();
        
        return (short)((b2 & 0xFF) << 8 | b1 & 0xFF);
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
    catch (IndexOutOfBoundsException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static int readInt(InputStream stream) {
    try {
        int b1 = stream.read();
        int b2 = stream.read();
        int b3 = stream.read();
        int b4 = stream.read();
        
        return (b4 & 0xFF) << 24 | (b3 & 0xFF) << 16 | (b2 & 0xFF) << 8 | b1 & 0xFF;
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
    catch (IndexOutOfBoundsException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static int readIntFromBytes(byte[] intBytes) {
    return (intBytes[3] & 0xFF) << 24 | (intBytes[2] & 0xFF) << 16 |
           (intBytes[1] & 0xFF) << 8 | intBytes[0] & 0xFF;
}

public static long readLong(InputStream stream) {
    try {
        byte[] long_buf = new byte[8];
        stream.read(long_buf);
        ByteBuffer bb = ByteBuffer.wrap(long_buf, 0, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
    catch (IndexOutOfBoundsException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static long readUnsignedInt(InputStream stream) {
    try {
        byte[] uint_buf = new byte[4];
        stream.read(uint_buf);
        ByteBuffer bb = ByteBuffer.wrap(uint_buf, 0, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        
        return bb.getInt() & 0xFFFFFFFF;
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
    catch (IndexOutOfBoundsException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static double readDouble(InputStream stream) {
    try {
        byte[] double_buf = new byte[8];
        stream.read(double_buf);
        ByteBuffer bb = ByteBuffer.wrap(double_buf, 0, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getDouble();
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
    catch (IndexOutOfBoundsException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static byte[] readId(InputStream stream) {
    try {
        byte[] id_buf = new byte[16];
        stream.read(id_buf);
        
        return id_buf;
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static String readBSTR(InputStream stream) {
    int bytes = readInt(stream);
    return readFixedString(stream, bytes / 2);
}

public static String readFixedString(InputStream stream, int len) {
    try {
        if (len == 0) {
            return null;
        }
        
        byte[] string_buf = new byte[len * 2];
        stream.read(string_buf);
        
        ByteBuffer bb = ByteBuffer.wrap(string_buf);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.asCharBuffer().toString();
    }
    catch (IOException e) {
        throw new RuntimeException("E_UTIL_LITTLE_ENDIAN_CONVERSION_FAILED", e);
    }
}

public static String ConvertUUIDToGUID(String guid) {
    if (guid.contains("-")) {
        guid = guid.replace("-", "");
        guid = guid.toUpperCase();
    }
    
    String s1 = guid.substring(0, 2);
    String s2 = guid.substring(2, 4);
    String s3 = guid.substring(4, 6);
    String s4 = guid.substring(6, 8);
    String s5 = guid.substring(8, 10);
    String s6 = guid.substring(10, 12);
    String s7 = guid.substring(12, 14);
    String s8 = guid.substring(14, 16);
    String s9 = guid.substring(16, 20);
    String s10 = guid.substring(20);
    
    return "{" + s4 + s3 + s2 + s1 + "-" + s6 + s5 +
           "-" + s8 + s7 + "-" + s9 + "-" + s10 + "}";
}
