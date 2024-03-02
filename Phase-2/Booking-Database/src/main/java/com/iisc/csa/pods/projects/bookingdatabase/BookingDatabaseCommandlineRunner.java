package com.iisc.csa.pods.projects.bookingdatabase;

import com.iisc.csa.pods.projects.bookingdatabase.model.Show;
import com.iisc.csa.pods.projects.bookingdatabase.model.Theatre;
import com.iisc.csa.pods.projects.bookingdatabase.repository.ShowRepository;
import com.iisc.csa.pods.projects.bookingdatabase.repository.TheatreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class BookingDatabaseCommandlineRunner implements CommandLineRunner {
    // Resource loader instance to access theatres.csv and shows.csv
    @Autowired
    private ResourceLoader resourceLoader;
    // Repository instance for Theatre and Shows entities
    private final TheatreRepository theatreRepo;
    private final ShowRepository showRepo;


    public BookingDatabaseCommandlineRunner(TheatreRepository theatreRepo_, ShowRepository showRepo_){
        this.theatreRepo = theatreRepo_;
        this.showRepo = showRepo_;
    }

    @Override
    public void run (String... args) throws Exception {
        System.out.println("Running DB initialization.");
        PopulateTheatreTable();
        PopulateShowsTable();
    }


    /**
     * Constants to handle CSV indices/columns of Theatres.csv file
     */
    private final Integer THEATRE_CSV_IDX_ID = 0;
    private final Integer THEATRE_CSV_IDX_NAME = 1;
    private final Integer THEATRE_CSV_IDX_LOCATION = 2;

    /**
     * Method to populate Theatre Entity from theatres.csv.
     *
     * @throws IOException
     */
    private void PopulateTheatreTable() throws IOException {
        final Resource theatresFileResource = resourceLoader.getResource("classpath:static/theatres.csv");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader( theatresFileResource.getInputStream()));
            String line;
            Integer lineCtr = 0;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                String [] values = line.split(",");
                if (lineCtr > 0 ){
                    // Skip the header line in CSV
                    Theatre newRec = new Theatre(Integer.parseInt(values[THEATRE_CSV_IDX_ID]),
                            values[THEATRE_CSV_IDX_NAME],
                            values[THEATRE_CSV_IDX_LOCATION]);
                    this.theatreRepo.save(newRec);
                }
                lineCtr++;
            }
        } catch (IOException e) {}

    }

    /**
     * Constants to handle CSV indices/columsn of Theatres.csv file
     */
    private final Integer SHOWS_CSV_IDX_ID = 0;
    private final Integer SHOWS_CSV_IDX_THEATRE_ID = 1;
    private final Integer SHOWS_CSV_IDX_TITLE = 2;
    private final Integer SHOWS_CSV_IDX_PRICE = 3;
    private final Integer SHOWS_CSV_IDX_SEATS_AVAILABLE = 4;

    /**
     * Method to populate Shows Entity from shows.csv.
     *
     * @throws IOException
     */
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
                    System.out.println("Record = "+values[0]+" ; "+values[1]+" ; "+values[2]);
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
