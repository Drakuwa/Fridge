package com.app.afridge.api;

import com.app.afridge.dom.IngredientHelper;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;


/**
 * API calls
 * <p/>
 * Created by drakuwa on 1/30/15.
 */
public class RestService {

  // create service objects from the interfaces
  public FCService fcService;

  public RestService(RestAdapter restAdapter) {

    fcService = restAdapter.create(FCService.class);
  }

  /**
   * FridgeCheck service
   */
  public interface FCService {

    // GET /ing.php - get an ingredients list
    @GET("/ing.php")
    void getIngredients(Callback<List<IngredientHelper>> callback);
  }
}
