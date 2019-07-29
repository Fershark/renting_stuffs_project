package com.quangle.rentingutilities.viewmodel;

import com.quangle.rentingutilities.core.model.Auth;
import com.quangle.rentingutilities.core.model.Booking;
import com.quangle.rentingutilities.core.model.Item;
import com.quangle.rentingutilities.networking.Api;
import com.quangle.rentingutilities.networking.NetworkResource;
import com.quangle.rentingutilities.networking.RetrofitService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemViewModel extends ViewModel {
    MutableLiveData<NetworkResource<Item>> networkResourceItemMutableLiveData = new MutableLiveData<>();
    MutableLiveData<List<Item>> mItems = new MutableLiveData<>();
    MutableLiveData<List<Item>> mUserItems = new MutableLiveData<>();//items of logged in user
    MutableLiveData<List<Item>> mUserWishList = new MutableLiveData<>();//wishlist of logged in user
    MutableLiveData<List<Booking>> mUserBookings = new MutableLiveData<>();//bookings of logged in user
    MutableLiveData<NetworkResource<Booking>> networkResourceBookingMutableLiveData = new MutableLiveData<>();
    Api api;
    private FirebaseAuth firebaseAuth;

    public ItemViewModel() {
        if (api == null) {
            api = RetrofitService.get();
        }

        if (firebaseAuth ==null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
    }

    public LiveData<List<Item>> getAllItems() {
        Call<List<Item>> itemCall = api.getAllItems();
        itemCall.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                mItems.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                System.out.println("ON FAILURE");
                System.out.println(t.getCause());

                // get token

            }
        });

        return mItems;
    }

    public LiveData<List<Item>> getAllItemsOfUser() {
        firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            Call<List<Item>> itemCall = api.getAllItemsOfUser(getTokenResult.getToken());
            itemCall.enqueue(new Callback<List<Item>>() {
                @Override
                public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                    if (response.isSuccessful()) {
                        mUserItems.setValue(response.body());
                    } else {
                        System.out.println("DFdfd");
                    }
                }

                @Override
                public void onFailure(Call<List<Item>> call, Throwable t) {
                    System.out.println("ON FAILURE");
                    System.out.println(t.getCause());
                }
            });
        });

        return mUserItems;
    }

    public LiveData<List<Item>> getWishlistOfUser(Auth auth) {

        firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            Call<List<Item>> itemCall = api.getWishlistOfUser(getTokenResult.getToken());
            itemCall.enqueue(new Callback<List<Item>>() {
                @Override
                public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                    if (response.isSuccessful()) {
                        mUserWishList.setValue(response.body());
                    } else {
                        // TODO: Token Expired, Display message Redirect to LOGIN activity
                    }
                }

                @Override
                public void onFailure(Call<List<Item>> call, Throwable t) {
                    System.out.println("ON FAILURE");
                    System.out.println(t.getCause());
                }
            });
        });

        return mUserWishList;
    }

    public LiveData<List<Booking>> getUserBookings(Auth auth) {

        firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            Call<List<Booking>> bookingCall = api.getBookingsOfUser(getTokenResult.getToken());
            bookingCall.enqueue(new Callback<List<Booking>>() {
                @Override
                public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                    getBookingsWithItemDetail(getTokenResult.getToken(), response.body());
                }

                @Override
                public void onFailure(Call<List<Booking>> call, Throwable t) {
                    System.out.println("ON FAILURE");
                    System.out.println(t.getCause());
                }
            });
        });

        return mUserBookings;
    }

    private void getBookingsWithItemDetail(String accessToken, List<Booking> bookings) {
        List<Booking> tempBookings = new ArrayList<>();

        for(Booking b: bookings) {
            Booking temp = new Booking();
            temp.setId(b.getId());
            temp.setBorrowerId(b.getBorrowerId());
            temp.setItemId(b.getItemId());
            temp.setStartDate(b.getStartDate());
            temp.setReturnDate(b.getReturnDate());
            temp.setStatus(b.getStatus());

            Call<Item> callItem = api.getItemDetail(accessToken, String.valueOf(b.getItemId()));
            callItem.enqueue(new Callback<Item>() {
                @Override
                public void onResponse(Call<Item> call, Response<Item> response) {
                    temp.setItem(response.body());
                    tempBookings.add(temp);
                    mUserBookings.setValue(tempBookings);
                }

                @Override
                public void onFailure(Call<Item> call, Throwable t) {
                    System.out.println("HERE FAILURE");
                    System.out.println(t.getCause());
                }
            });
            System.out.println("HERE " + String.valueOf(b.getItemId()));
        }

    }

    public LiveData<NetworkResource<Item>> createItem(MultipartBody.Part filePart, HashMap<String, RequestBody> params) {
        firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            Api api = RetrofitService.get();
            Call<Item> itemCall = api.createItem(getTokenResult.getToken(), filePart, params);
            itemCall.enqueue(new Callback<Item>() {
                @Override
                public void onResponse(Call<Item> call, Response<Item> response) {
                    if (response.isSuccessful())
                        networkResourceItemMutableLiveData.setValue(new NetworkResource<>(response.body()));
                    else
                        networkResourceItemMutableLiveData.setValue(new NetworkResource<>(response.code()));
                }

                @Override
                public void onFailure(Call<Item> call, Throwable t) {
                    System.out.println("ON FAILURE");
                    System.out.println(t.getStackTrace());
                }
            });
        });

        return networkResourceItemMutableLiveData;
    }

    public LiveData<NetworkResource<Item>> editItem(MultipartBody.Part filePart, String itemId, HashMap<String, RequestBody> params) {
        firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            Api api = RetrofitService.get();
            System.out.println("Test" + itemId);
            Call<Item> itemCall = api.editItem(getTokenResult.getToken(), itemId, filePart, params);
            itemCall.enqueue(new Callback<Item>() {
                @Override
                public void onResponse(Call<Item> call, Response<Item> response) {
                    if (response.isSuccessful())
                        networkResourceItemMutableLiveData.setValue(new NetworkResource<>(response.body()));
                    else
                        networkResourceItemMutableLiveData.setValue(new NetworkResource<>(response.code()));
                }

                @Override
                public void onFailure(Call<Item> call, Throwable t) {
                    System.out.println("ON FAILURE");
                    System.out.println(t.getStackTrace());
                }
            });
        });
        return networkResourceItemMutableLiveData;
    }

    //create booking
    public LiveData<NetworkResource<Booking>> createBooking(HashMap<String, Object> params) {
        firebaseAuth.getCurrentUser().getIdToken(false).addOnSuccessListener(getTokenResult -> {
            Api api = RetrofitService.get();
            Call<Booking> bookingCall = api.createBooking(getTokenResult.getToken(), params);
            bookingCall.enqueue(new Callback<Booking>() {
                @Override
                public void onResponse(Call<Booking> call, Response<Booking> response) {
                    if (response.isSuccessful())
                        networkResourceBookingMutableLiveData.setValue(new NetworkResource<>(response.body()));
                    else
                        networkResourceBookingMutableLiveData.setValue(new NetworkResource<>(response.code()));
                }

                @Override
                public void onFailure(Call<Booking> call, Throwable t) {
                    System.out.println("ON FAILURE");
                    System.out.println(t.getStackTrace());
                }
            });
        });
        return networkResourceBookingMutableLiveData;
    }
}
