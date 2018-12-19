package daymos.lodz.uni.math.pl.socialapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;


import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {



    private CallbackManager mCallbackManager;

    private static final String TAG = "FACELOG";
    private static Activity activity;
    private ImageView avatar;
    private TextView FirstName;
    private TextView LastName;
    private TextView email;
    private TextView gender;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("SocialApp");
        avatar = findViewById(R.id.imageView);
        FirstName = findViewById(R.id.txtFirstName);
        LastName = findViewById(R.id.txtLastName);
        email = findViewById(R.id.txtEmail);
        gender = findViewById(R.id.txtGender);








        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject jsonObject,
                                                    GraphResponse response) {

                                // Getting FB User Data
                                getFacebookData(jsonObject);
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender");
                request.setParameters(parameters);
                request.executeAsync();

                Toast.makeText(MainActivity.this, "Udalo sie zalogowac ", Toast.LENGTH_LONG).show();


            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }




    private void getFacebookData(JSONObject object) {
        Bundle bundle = new Bundle();

        try {
            String id = object.getString("id");
            URL profile_pic;
            try {
                profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
                Log.i("profile_pic", profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());
                Toast.makeText(MainActivity.this,bundle.getString("profile_pic"),Toast.LENGTH_LONG).show();
                Picasso.with(this).load(profile_pic.toString()).into(avatar);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
                FirstName.setText(object.getString("first_name"));

            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
                LastName.setText(object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
                email.setText(object.getString("email"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
                //gender.setText(object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
                gender.setText(object.getString("birthday"));



        } catch (Exception e) {
            Log.d(TAG, "BUNDLE Exception : " + e.toString());
        }



    }

}
