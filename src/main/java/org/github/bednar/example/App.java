package org.github.bednar.example;

import java.time.Instant;
import java.util.List;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

/**
 * Hello world!
 */
public class App {
    private static final String URL = "http://localhost:8086";
    private static final String TOKEN = "my-token";
    private static final String ORG = "my-org";
    private static final String BUCKET = "my-bucket";

    public static void main(String[] args) {
        try (InfluxDBClient client = InfluxDBClientFactory.create(URL, TOKEN.toCharArray(), ORG, BUCKET)) {

            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            QueryApi queryApi = client.getQueryApi();

            //
            // Write data
            //
            Point point = Point.measurement("temperature")
                    .addTag("location", "west")
                    .addField("last_value", 55)
                    .time(Instant.now().toEpochMilli(), WritePrecision.MS);

            writeApi.writePoint(point);

            //
            // Query data
            //
            String flux = String.format("from(bucket:\"%s\") |> range(start: 0)", BUCKET);
            List<FluxTable> tables = queryApi.query(flux);
            for (FluxTable fluxTable : tables) {
                List<FluxRecord> records = fluxTable.getRecords();
                for (FluxRecord fluxRecord : records) {
                    System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
                }
            }
        }
    }
}
