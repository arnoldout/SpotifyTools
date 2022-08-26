import com.neovisionaries.i18n.CountryCode;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchAlbumsRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlaylistExpander {
    public static String playlistId = "6wVclVqO1AVPhKYwaME7IJ";

    public static void main(String[] args) throws IOException, ParseException, SpotifyWebApiException {
        List<String> newPlaylist = printSpotifyStuff();
        Collections.shuffle(newPlaylist);
        for (String s : newPlaylist) {
            System.out.println(s);
        }
    }
    private static List<String> printSpotifyStuff() throws IOException, ParseException, SpotifyWebApiException {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId("40eb58750b2246fa910a6bc659b91003")
                .setClientSecret("ce89b6849eea449798a6f47a0dfbc767")
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        final ClientCredentials clientCredentials = clientCredentialsRequest.execute();

        // Set access token for further "spotifyApi" object usage
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        List<String> newPlaylist = new ArrayList<>();

        String id = "6nWC36F2Xkql1akZ6qy7yn";
        Track thisTrack = spotifyApi.getTrack(id)
                .market(CountryCode.IE)
                .build().execute();


        final Playlist req = spotifyApi.getPlaylist(playlistId)
                .market(CountryCode.IE)
                .build().execute();
        Set<String> albumIds = new HashSet<>();
        for (PlaylistTrack track : req.getTracks().getItems())
        {
            final Track track1 = spotifyApi.getTrack(track.getTrack().getId())
                    .market(CountryCode.IE)
                    .build().execute();
            if(!albumIds.contains(track1.getAlbum().getId()))
            {
                albumIds.add(track1.getAlbum().getId());
                final Album album = spotifyApi.getAlbum(track1.getAlbum().getId())
                        .market(CountryCode.IE)
                        .build().execute();
                newPlaylist.addAll(Arrays.stream(album.getTracks().getItems()).map(urEntity -> urEntity.getExternalUrls().get("spotify")).collect(Collectors.toList()));

            }
        }
        return newPlaylist;
    }
}
