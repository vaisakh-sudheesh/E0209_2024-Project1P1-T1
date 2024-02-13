package com.iisc.csa.pods.projects.booking;

import com.iisc.csa.pods.projects.booking.model.Show;
import com.iisc.csa.pods.projects.booking.model.Theatre;
import com.iisc.csa.pods.projects.booking.repository.ShowRepository;
import com.iisc.csa.pods.projects.booking.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Component
public class BookingCommandLineRunner implements CommandLineRunner {
    private final TheatreRepository theatreRepo;
    private final ShowRepository showRepo;

    @Autowired
    private ResourceLoader resourceLoader;

    public BookingCommandLineRunner(TheatreRepository theatreRepo_, ShowRepository showRepo_){

        this.theatreRepo = theatreRepo_;
        this.showRepo = showRepo_;
    }

    @Override
    public void run (String... args) throws Exception {
        PopulateTheatreTable();
        PopulateShowsTable();
    }

    private final  Integer THEATRE_CSV_IDX_ID = 0;
    private final  Integer THEATRE_CSV_IDX_NAME = 1;
    private final Integer THEATRE_CSV_IDX_LOCATION = 2;
    private void PopulateTheatreTable() throws IOException {
        final Resource theatresFileResource = resourceLoader.getResource("classpath:static/theatres.csv");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader( theatresFileResource.getInputStream()));
            String line;
            Integer lineCtr = 0;
            while ((line = br.readLine()) != null) {
                String [] values = line.split(",");
                if (lineCtr > 0 ){
                    // Skip the header line in CSV
                    // System.out.println("Record = "+values[0]+" ; "+values[1]+" ; "+values[2]);
                    Theatre newRec = new Theatre(Integer.parseInt(values[THEATRE_CSV_IDX_ID]),
                                                values[THEATRE_CSV_IDX_NAME],
                                                values[THEATRE_CSV_IDX_LOCATION]);
                    this.theatreRepo.save(newRec);
                }
                lineCtr++;
            }
        } catch (IOException e) {}

    }

    private final Integer SHOWS_CSV_IDX_ID = 0;
    private final Integer SHOWS_CSV_IDX_THEATRE_ID = 1;
    private final Integer SHOWS_CSV_IDX_TITLE = 2;
    private final Integer SHOWS_CSV_IDX_PRICE = 3;
    private final Integer SHOWS_CSV_IDX_SEATS_AVAILABLE = 4;

    private void PopulateShowsTable()  throws IOException{
        final Resource showsFileResource = resourceLoader.getResource("classpath:static/shows.csv");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader( showsFileResource.getInputStream()));
            String line;
            Integer lineCtr = 0;
            while ((line = br.readLine()) != null) {
                String [] values = line.split(",");
                if (lineCtr > 0 ){
                    // Skip the header line in CSV
                    // System.out.println("Record = "+values[0]+" ; "+values[1]+" ; "+values[2]);
                    Show newRec = new Show(Integer.parseInt(values[SHOWS_CSV_IDX_ID]),
                                            Integer.parseInt(values[SHOWS_CSV_IDX_THEATRE_ID]),
                                            values[SHOWS_CSV_IDX_TITLE],
                                            Integer.parseInt(values[SHOWS_CSV_IDX_PRICE]),
                                            Integer.parseInt(values[SHOWS_CSV_IDX_SEATS_AVAILABLE]));
                    this.showRepo.save(newRec);
                }
                lineCtr++;
            }
        } catch (IOException e) {}

    }
}
