package org.example.supportflow.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import org.example.supportflow.R;
import org.example.supportflow.data.TicketRepository;
import org.example.supportflow.model.Ticket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReportesActivity extends AppCompatActivity {

    private LineChart lineChart;
    private PieChart pieChart;
    private BarChart barChart;
    private final TicketRepository repo = new TicketRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reportes);

        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        cargarDatos();
    }

    private void cargarDatos() {
        repo.escucharTodosLosTickets(new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                mostrarGraficaLinea(tickets);
                mostrarGraficaPastel(tickets);
                mostrarGraficaBarras(tickets);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminReportesActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarGraficaLinea(List<Ticket> tickets) {
        // Tickets por día (últimos 7 días simulados con índice)
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < tickets.size() && i < 7; i++) {
            entries.add(new Entry(i, 1));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Tickets");
        dataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        dataSet.setCircleColor(ColorTemplate.MATERIAL_COLORS[0]);

        lineChart.setData(new LineData(dataSet));
        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }

    private void mostrarGraficaPastel(List<Ticket> tickets) {
        // Contar tickets por categoría
        Map<String, Integer> categorias = new HashMap<>();
        for (Ticket t : tickets) {
            String cat = t.getCategory() != null ? t.getCategory() : "Sin categoría";
            categorias.put(cat, categorias.getOrDefault(cat, 0) + 1);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categorias.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Categorías");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        pieChart.setData(new PieData(dataSet));
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    private void mostrarGraficaBarras(List<Ticket> tickets) {
        // Contar tickets por técnico asignado
        Map<String, Integer> tecnicos = new HashMap<>();
        for (Ticket t : tickets) {
            if (t.getAssignedTo() != null && !t.getAssignedTo().isEmpty()) {
                tecnicos.put(t.getAssignedTo(), tecnicos.getOrDefault(t.getAssignedTo(), 0) + 1);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : tecnicos.entrySet()) {
            entries.add(new BarEntry(i++, entry.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tickets por técnico");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        barChart.setData(new BarData(dataSet));
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }
}