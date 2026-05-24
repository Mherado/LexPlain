package mher.minasyan.lexplain;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import java.util.List;

public interface HuggingFaceApi {
    @POST("v1/chat/completions")
    Call<HuggingFaceResponse> summarize(
            @Header("Authorization") String token,
            @Body HuggingFaceRequest request
    );
}