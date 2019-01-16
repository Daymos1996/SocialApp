package daymos.lodz.uni.math.pl.socialapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {



    private CallbackManager mCallbackManager;

    private static final String TAG = "FACELOG";
    private ImageView avatar;
    private TextView FirstName;
    private TextView LastName;
    private TextView email;
    private Button shareLink;
    private Button sharePhoto;
    private Button shareVideo;
    private String Accesstoken;
    public static final int PICK_IMAGE = 1;
    public static final int PICK_VIDEO = 2;
    private ProgressDialog mProgresDiaolog;
    ShareDialog shareDialog;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        mCallbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        mProgresDiaolog = new ProgressDialog(this);



        final LoginButton loginButton = findViewById(R.id.login_button);
       // loginButton.setReadPermissions("email", "public_profile","user_birthday","user_friends");
        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                Accesstoken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject jsonObject,
                                                    GraphResponse response) {

                                // Getting FB User Data
                                getFacebookData(jsonObject);
                                shareLink.setVisibility(View.VISIBLE);
                                sharePhoto.setVisibility(View.VISIBLE);
                                shareVideo.setVisibility(View.VISIBLE);

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email");
                request.setParameters(parameters);
                request.executeAsync();



                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LoginManager.getInstance().logOut();
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

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


        shareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setQuote("this is useful link")
                        .setContentUrl(Uri.parse("https://youtube.com"))
                        .build();
                if(ShareDialog.canShow(ShareLinkContent.class)){
                    shareDialog.show(linkContent);
                }
            }
        });

        sharePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPhotoChooser();

            }
        });



        shareVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openVideoChooser();
            }
        });
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);



            if(requestCode == PICK_VIDEO ){
            Uri selectedVideo = data.getData();

                ShareVideo video = new ShareVideo.Builder()
                        .setLocalUrl(selectedVideo)
                        .build();

                ShareVideoContent videoContent = new ShareVideoContent.Builder()
                        .setContentTitle("Moje film")
                        .setContentDescription("moje ostatnie wspomnienia")
                        .setVideo(video)
                        .build();

                if(shareDialog.canShow(ShareVideoContent.class))
                    shareDialog.show(videoContent);
            }

        if (requestCode == PICK_IMAGE) {
            Uri mImageProfileUri = data.getData();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageProfileUri);
                
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(image)
                        .build();

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                if(ShareDialog.canShow(SharePhotoContent.class)){
                    shareDialog.show(content);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
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




        } catch (Exception e) {
            Log.d(TAG, "BUNDLE Exception : " + e.toString());
        }



    }
    private void openPhotoChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        // intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, PICK_IMAGE);
    }
    private void openVideoChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO);

    }




        private void init() {
        setTitle("SocialApp");
        avatar = findViewById(R.id.imageView);
        FirstName = findViewById(R.id.txtFirstName);
        LastName = findViewById(R.id.txtLastName);
        email = findViewById(R.id.txtEmail);
        shareLink = (Button) findViewById(R.id.buttonShareLink);
        sharePhoto = (Button) findViewById(R.id.buttonSharePhoto);
        shareVideo = (Button) findViewById(R.id.buttonShareVideo);

    }
}
