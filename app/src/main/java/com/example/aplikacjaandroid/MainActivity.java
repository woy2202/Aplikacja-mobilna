package com.example.aplikacjaandroid;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView cityName;
    Button search;
    TextView show;
    TextView temp;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        search = findViewById(R.id.search);
        show = findViewById(R.id.weather);
        temp = findViewById(R.id.temp);  // Dodana referencja do nowego TextView

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityName.getText().toString().trim();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Wpisz nazwę miasta", Toast.LENGTH_SHORT).show();
                    return;
                }

                url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=3b104e9d5c311f385c7c88d868bc8b84&units=metric";

                executorService.execute(() -> {
                    try {
                        StringBuilder result = new StringBuilder();
                        URL apiUrl = new URL(url);
                        HttpURLConnection urlConnection = (HttpURLConnection) apiUrl.openConnection();
                        urlConnection.connect();

                        InputStream inputStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line).append("\n");
                        }

                        String weatherData = result.toString();

                        // Przetwarzanie JSON i aktualizacja UI
                        mainHandler.post(() -> {
                            try {
                                JSONObject jsonObject = new JSONObject(weatherData);
                                JSONObject main = jsonObject.getJSONObject("main");
                                double temperature = main.getDouble("temp");
                                double feelsLike = main.getDouble("feels_like");
                                double tempMax = main.getDouble("temp_max");
                                double tempMin = main.getDouble("temp_min");
                                int pressure = main.getInt("pressure");
                                int humidity = main.getInt("humidity");

                                String weatherInfo = String.format(
                                        "Odczuwalna: %.1f°C\n" +
                                                "Temperatura max.: %.1f°C\n" +
                                                "Temperatura min.: %.1f°C\n" +
                                                "Ciśnienie: %d hPa\n" +
                                                "Wilgotność: %d%%",
                                        feelsLike, tempMax, tempMin, pressure, humidity
                                );

                                show.setText(weatherInfo);
                                temp.setText(String.format("%.1f°C", temperature));  // Ustawienie temperatury

                            } catch (Exception e) {
                                e.printStackTrace();
                                show.setText("Nie udało się przetworzyć danych pogodowych.");
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        mainHandler.post(() -> {
                            show.setText("Nie udało się pobrać danych pogodowych.");
                        });
                    }
                });
            }
        });
    }
}
