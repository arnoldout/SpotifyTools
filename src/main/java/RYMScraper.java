import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RYMScraper {

    private static final String url = "https://rateyourmusic.com/list/ukrayf/some-come-unsung-sorry-no-song-for-some-sing-sad-song-sung/";

    public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
        List<String> searchQueries = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements table = doc.select("#user_list"); //select the first table.
        Elements oddRows = table.select("tr.trodd");
        Elements evenRows = table.select("tr.treven");
        searchQueries.addAll(getQueries(oddRows));
        searchQueries.addAll(getQueries(evenRows));
        List<String> responses = new ArrayList<>();
        for(String query : searchQueries) {
            responses.addAll(printSpotifyStuff(query));
        }
        Collections.shuffle(responses);
        for (String s : responses) {
            System.out.println(s);
        }
    }

    private static List<String> getQueries(Elements oddRows) {
        List<String> searchQueries = new ArrayList<>();
        for (int i = 0; i < oddRows.size(); i++) { //first row is the col names so skip it.
            Element row = oddRows.get(i);
            String artist = row.select("td:nth-child(3) > h2:nth-child(1) > a:nth-child(2)").text();
            String album = row.select("td:nth-child(3) > h2:nth-child(1) > a:nth-child(2)").text();
            if(!(artist.isEmpty()||album.isEmpty()))
            {
                searchQueries.add(artist+" "+album);
            }
        }
        return searchQueries;
    }

    private static List<String> printSpotifyStuff(String search) throws IOException, ParseException, SpotifyWebApiException {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId("40eb58750b2246fa910a6bc659b91003")
                .setClientSecret("ce89b6849eea449798a6f47a0dfbc767")
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

        // Set access token for further "spotifyApi" object usage
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        final SearchAlbumsRequest req = spotifyApi.searchAlbums(search)
                .market(CountryCode.IE)
                .build();

        try {
            // Execute the request synchronous
            final Paging<AlbumSimplified> response = req.execute();

            String id = Arrays.stream(response.getItems()).findFirst().get().getId();

            if(id!=null && !(id.isEmpty()))
            {
                final GetAlbumsTracksRequest req2 = spotifyApi.getAlbumsTracks(id)
                        .market(CountryCode.IE)
                        .build();

                final Paging<TrackSimplified> response2 = req2.execute();
                List<String> field1List = Arrays.stream(response2.getItems()).map(urEntity -> urEntity.getExternalUrls().get("spotify")).collect(Collectors.toList());
                return field1List;
            }
        } catch (Exception e) {
            System.out.println("Something went wrong!\n" + e.getMessage());
        }
        return new ArrayList<>();
    }

}
