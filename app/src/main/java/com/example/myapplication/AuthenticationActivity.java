package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.amazonaws.amplify.generated.graphql.CreatePetMutation;
import com.amazonaws.amplify.generated.graphql.GetCustomersQuery;
import com.amazonaws.amplify.generated.graphql.GetEmployeesQuery;
import com.amazonaws.amplify.generated.graphql.GetPetQuery;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.Credentials;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.myapplication.EmployeeMenu.EmployeeMenu;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.Map;
import java.util.Timer;

import javax.annotation.Nonnull;

import type.CreatePetInput;

public class AuthenticationActivity extends AppCompatActivity {

    private final String TAG = AuthenticationActivity.class.getSimpleName();
    boolean loggedIn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        //customer
        //cust12345
        //There are 3 types of users -> admin/employee/customer
        //Admin can view the application as any type of user
        //The following will forward the admin to the desired view
        //In the final app version well probably make the admin logout to go back to admin view.
        //That way the customer/employee menu does'nt have "View as..." options in its toolbar.
        Log.i("[][][][][][][][][][][][][][]","$$$$$$$$$$$$$$$");
//        showSignIn();
        //AWSMobileClient.getInstance().signOut();

        if(currentUser.loggingIn) {

        }


        if(currentUser.admin){
            Log.i("ADMIN$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$","$$$$$$$$$$$$$$$");

            if(currentUser.customer){
                //currentUser.customer=false;
                logInAsCustomer();
            }
            else if(currentUser.employee){
                //currentUser.employee=false;
                logInAsEmployee();
            }
            else {
                logInAsAdmin();
            }

        }
        Log.i("[1][1][1][1][1][1][1][][][][][][][]","$$$$$$$$$$$$$$$");
        ClientFactory.init(this);
        Log.i("[4][4][4][4][4][4][4][][][][][][][]","$$$$$$$$$$$$$$$");
        if(currentUser.loggingOut){
            Log.i("ADMIN$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$","$$$$$$$$$$$$$$$");
            Intent nextIntent = new Intent(getApplicationContext(), AuthenticationActivity.class);
            ProcessPhoenix.triggerRebirth(AuthenticationActivity.this, nextIntent);
            //showSignIn2();
        }
        //while(!loggedIn) {
        //if(!loggedIn) {
            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {

                    //AWSMobileClient.getInstance().signOut();

                    loggedIn = true;
                    Log.i(TAG, userStateDetails.getUserState().toString());
                    Log.i("[2][2][2][2][2][2][2][][][][][][][]", "$$$$$$$$$$$$$$$");

                    switch (userStateDetails.getUserState()) {
                        case SIGNED_IN:
                            //Only pull data if it is our first time logging in.

                            if (!currentUser.hasData) {
                                try {
                                    //Pull user data from Cognito and save it locally.
                                    pullUserData();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }



                                //Check our 3 DB tables to see what type of user is logging in.
                                Log.i("MAIN MENU", "WE ARE BACK");

                                isAdmin(currentUser.id);

                                isEmployee(currentUser.id);

                                isCustomer(currentUser.id);

                            }
                            break;
                        case SIGNED_OUT:

                            Log.i("++++++++++++++++++++++++++++++++++++++++++++++++++++", "$$$$$$$$$$$$$$$");

                            showSignIn2();
                            //finish();
                            //AuthenticationActivity.this.finish();
                            break;
                        default:
                            AWSMobileClient.getInstance().signOut();
                            showSignIn();
                            break;
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.toString());
                    Log.i("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEERORRR", "$$$$$$$$$$$$$$$");
                    showSignIn2();


                }
            });
        }
    //======================================================================================showSignIn
    //This function is called when the user is signed out.
    //The .nextActivity() might need to be this activity, so the user info can be viewed.
    private void showSignIn2() {
        try {
            CognitoCachingCredentialsProvider provider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-2_si1cYK2IO",
                    Regions.US_EAST_2);
            provider.clear();

           // provider.refresh();
            currentUser.loggingIn=true;
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder().nextActivity(AuthenticationActivityLoading.class).build());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    //======================================================================================showSignIn

    //======================================================================================isAdmin
    //Check to see if the current user is in the admin table.
    //Im using the "Pet" table for now, will replace later. So pet==admin
    public void isAdmin(String myID) {
        Log.i("##########################################", "ADMIN");
        ClientFactory.appSyncClient().query(GetPetQuery.builder().id(myID).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback3);
    }
    private GraphQLCall.Callback<GetPetQuery.Data>queryCallback3=new GraphQLCall.Callback<GetPetQuery.Data>() {
        @Override //final after nonnull
        public void onResponse(@Nonnull Response<GetPetQuery.Data> response) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
                    //Toast.makeText(AuthenticationActivity.this, "Updated admin view AUTH ACTIVITY", Toast.LENGTH_LONG).show();
                    //DisplayItems.this.finish();
                    Log.i("AUTHACT QUERY/////////////////////////////////////////////////", "RESPONSE RECIEVED");
                    Log.i("ADMIN", response.data().toString());
                    if(response.data().getPet()!=null) {
                        //admin = true;
                        if(currentUser.admin==false) {
                            Log.i("ADMIN", "###LEAVING");
                            currentUser.admin = true;
                            logInAsAdmin();
                        }
                    }
//                }
//            });

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i("AUTHACT QUERY", "RESPONSE not RECIEVED");

        }
    };
    //======================================================================================isAdmin

    //======================================================================================isEmployee
    //Check to see if the current user is in the employee table.
    public void isEmployee(String myID) {
        Log.i("##########################################", "EMP");
        ClientFactory.appSyncClient().query(GetEmployeesQuery.builder().id(myID).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }
    private GraphQLCall.Callback<GetEmployeesQuery.Data>queryCallback=new GraphQLCall.Callback<GetEmployeesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetEmployeesQuery.Data> response) {
            Log.i("AUTHACT QUERY", "RESPONSE RECIEVED");
            Log.i("EMPLOYEE", response.data().toString());
            if(response.data().getEmployees()!=null) {
                currentUser.employee=true;
                logInAsEmployee();
            }
            else{
                Log.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", "Not Employee");
            }
        }
        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i("AUTHACT QUERY", "RESPONSE not RECIEVED");

        }
    };
    //======================================================================================isEmployee

    //======================================================================================isCustomer
    //Check to see if the current user is in the customer table.
    public void isCustomer(String myID) {
//        ClientFactory.appSyncClient().query(GetCustomersQuery.builder().id("59a0f631-452c-4db7-a11f-6e5ef8adc1f6").build())
//                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
//                .enqueue(queryCallback2);
        Log.i("##########################################", "Customer");

        ClientFactory.appSyncClient().query(GetCustomersQuery.builder().id(myID).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback2);
    }
    private GraphQLCall.Callback<GetCustomersQuery.Data>queryCallback2=new GraphQLCall.Callback<GetCustomersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetCustomersQuery.Data> response) {
            Log.i("AUTHACT QUERY", "RESPONSE RECIEVED");
            Log.i("CUSTOMER", response.data().toString());
            if(response.data().getCustomers()!=null) {
                currentUser.customer=true;
                logInAsCustomer();
            }
            else{
                Log.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", "Not Customer");
                if(!currentUser.admin){
                    if(!currentUser.employee){
                        if(!currentUser.customer){
                            Intent intent = new Intent(AuthenticationActivity.this, AuthenticationActivityAddCustomer.class);
                            startActivity(intent);
                        }
                    }
                }

            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.i("AUTHACT QUERY", "RESPONSE not RECIEVED");

        }
    };
    //======================================================================================isCustomer

    public void logInAsCustomer(){
        Log.i("LOGING IN ","AS CUSTOMER");
        Intent intent = new Intent(AuthenticationActivity.this, CustomerMenu.class);
        startActivity(intent);
    }
    public void logInAsEmployee(){
        Log.i("LOGING IN ","AS EMPLOYEE");
        Intent intent = new Intent(AuthenticationActivity.this, EmployeeMenu.class);
        startActivity(intent);
    }
    public void logInAsAdmin(){
        Log.i("LOGING IN ","AS ADMIN");
        Intent intent = new Intent(AuthenticationActivity.this, AdminMenu.class);
        startActivity(intent);
    }
    //======================================================================================pullUserData
    //All of these functions are used to pull user information from AWS Cognito.
    public void pullUserData() throws Exception {
        currentUser.name= getCognitoName();
        currentUser.id= getCognitoID();
        currentUser.phone= getCognitoPhoneNumber();
        currentUser.email= getCognitoEmail();
        currentUser.hasData=true;
        Log.i("===========:", "User ID: "+currentUser.id+"\nName: "+currentUser.name+"\nPhone: "+currentUser.phone+"\nEmail: "+currentUser.email);
        //Run once to be an admin.
        //save();
    }
    public boolean getCognitoStatus(){
        return AWSMobileClient.getInstance().isSignedIn();
    }
    public String getCognitoName() throws Exception {
        Map m1=AWSMobileClient.getInstance().getUserAttributes();
        return m1.get("given_name").toString();
    }
    public String getCognitoID() throws Exception {
        Map m1=AWSMobileClient.getInstance().getUserAttributes();
        return m1.get("sub").toString();
    }
    public String getCognitoPhoneNumber() throws Exception {
        Map m1=AWSMobileClient.getInstance().getUserAttributes();
        return m1.get("phone_number").toString();
    }
    public String getCognitoEmail() throws Exception {
        Map m1=AWSMobileClient.getInstance().getUserAttributes();
        return m1.get("email").toString();
    }
    //======================================================================================pullUserData


    //======================================================================================showSignIn
    //This function is called when the user is signed out.
    //The .nextActivity() might need to be this activity, so the user info can be viewed.
    private void showSignIn() {
        try {
            if(currentUser.hasData) {
                currentUser.loggingIn = true;
            }
            currentUser.loggingIn = true;
            currentUser.hasData=false;

            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder().nextActivity(AuthenticationActivityAddCustomer.class).build());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Log.i("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& ","AS ADMIN");

        }
    }
    //======================================================================================showSignIn






    //=============================================================================CREATE
    private void save() {


        CreatePetInput input = CreatePetInput.builder()
                .id(currentUser.id)
                .name(currentUser.name)
                .description("admin")
                .build();
        CreatePetMutation addPetMutation = CreatePetMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(addPetMutation).enqueue(mutateCallback22);
    }

    // Mutation callback code
    private GraphQLCall.Callback<CreatePetMutation.Data> mutateCallback22 = new GraphQLCall.Callback<CreatePetMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreatePetMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(DisplayItems.this, "Added Item", Toast.LENGTH_SHORT).show();
                    // DisplayItems.this.finish();
                    Log.i("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS", "SSSSSSSSSSSSSSSSSSSSSSSSSSS");

                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddPetMutation", e);
                    //Toast.makeText(DisplayItems.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    //DisplayItems.this.finish();
                }
            });
        }
    };
//=============================================================================================CREATE



}









//Update user attributes
//                                Map m2= new HashMap();
//                                m2.put("given_name","cesar2");
//                                try {
//                                    AWSMobileClient.getInstance().updateUserAttributes(m2);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }



//Replaced with boolean in currentUser.
//    //======================================================================================Admin flag.
//    //Every time an admin wants to see the app from a customer/employee POV
//    //We need to switch the the flag from "customer"/"employee" back to "admin.
//    private void setAdminView(String id) {
//        UpdatePetInput input=UpdatePetInput.builder().id(id).description("admin").build();
//        UpdatePetMutation updatePetMutation= UpdatePetMutation.builder().input(input).build();
//        ClientFactory.appSyncClient().mutate(updatePetMutation).enqueue(mutateCallback4);
//    }
//
//    private GraphQLCall.Callback<UpdatePetMutation.Data> mutateCallback4 = new GraphQLCall.Callback<UpdatePetMutation.Data>() {
//        @Override
//        public void onResponse(@Nonnull final Response<UpdatePetMutation.Data> response) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$","FAX" +currentUser.id);
//
//                    Toast.makeText(AuthenticationActivity.this, "Updated admin view AUTH ACTIVITY", Toast.LENGTH_LONG).show();
//                    //DisplayItems.this.finish();
//                }
//            });
//        }
//
//        @Override
//        public void onFailure(@Nonnull final ApolloException e) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.e("", "Failed to perform UpdateItemMutation", e);
//                    Toast.makeText(AuthenticationActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
//                    //DisplayItems.this.finish();
//                }
//            });
//        }
//    };
////======================================================================================Admin flag.










//Is admin body code.
//
//
//
//                        adminView = response.data().getPet().description();
//                        Log.i("=====Resultss", adminView);
//
//                        //i2.putExtra("cogUser",currentUser.id);
//
//                        if(admin){
//                            Log.i("+++++++++++++++++++++++++++++++++++++++","FAX" +currentUser.id);
//
//                            if(adminView.equals("admin")){
//                                //setAdminView(currentUser.id);
//                                Log.i("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA","FAX" +currentUser.id);
//                                Intent i = new Intent(AuthenticationActivity.this, EmployeeMenu.class);
//
//                                startActivity(i);
//                                //AuthenticationActivity.this.finish();
//                            }
//                            if(adminView.equals("customer")){
//                                //setAdminView(currentUser.id);
//                                Log.i("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC","FAX" +currentUser.id);
//                                Intent c=new Intent(AuthenticationActivity.this,CustomerMenu.class);
//
//                                startActivity(c);
//                                //AuthenticationActivity.this.finish();
//
//                            }
//                            if(adminView.equals("employee")){
//                                //setAdminView(currentUser.id);
//                                Log.i("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE","FAX" +currentUser.id);
//                                Intent i2 = new Intent(AuthenticationActivity.this, EmployeeMenu.class);
//
//                                startActivity(i2);
//                                //AuthenticationActivity.this.finish();
//
//                            }
//                        }
//                        setAdminView(currentUser.id);



//Ignore.
//Line62 --begin onResult
////Toast.makeText(AuthenticationActivity.this, "Failed to delete item", Toast.LENGTH_LONG).show();
//String aws_cognito_region = "us-east-2"; // Replace this with your aws cognito region
//    String aws_user_pools_id = "us-east-2_si1cYK2IO"; // Replace this with your aws user pools id
//    RSAKeyProvider keyProvider = new AwsCognitoRSAKeyProvider(aws_cognito_region, aws_user_pools_id);
//    Algorithm algorithm = Algorithm.RSA256(keyProvider);
//    JWTVerifier jwtVerifier = JWT.require(algorithm)
//            //.withAudience("2qm9sgg2kh21masuas88vjc9se") // Validate your apps audience if needed
//            .build();
//
////String token = "eyJraWQiOiJjdE.eyJzdWIiOiI5NTMxN2E.VX819z1A1rJij2"; // Replace this with your JWT token
////                DecodedJWT jwt= jwtVerifier.verify(token);


//Case-SignedIn
//                        try {
//                                Log.i("PLSWORK:", "User Details"+ AWSMobileClient.getInstance().getUserAttributes().toString());
//
//                                //I/PLSWORK:: User Details{sub=167e820c-9c32-4ede-a084-b065bad7a55f, email_verified=true,
//                                // phone_number_verified=false, phone_number=+17146312899, given_name=cesar, email=cg000000@yahoo.com}
//                                } catch (Exception e) {
//                                e.printStackTrace();
//                                }

//End case-SignedIN
//
//                        try {
//
//
//                                Log.i("PLSWORK2:", "User Details"+ AWSMobileClient.getInstance().getTokens().getAccessToken().getTokenString());
//                                //I/PLSWORK2:: User DetailseyJraWQiOiJmb21TYzRUYnhyVnBnZXZiN2dkZERCUHJ0WXNSUlVIb2twajBQQSt0a0YwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIxNjdlODIwYy05YzMyLTRlZGUtYTA4NC1iMDY1YmFkN2E1NWYiLCJjb2duaXRvOmdyb3VwcyI6WyJXTVNBZG1pbiJdLCJldmVudF9pZCI6ImQ2MTVhNThjLWE0MTQtNGM4NC1hYTk1LWNjMTljMjkxMTkyNCIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG8uc2lnbmluLnVzZXIuYWRtaW4iLCJhdXRoX3RpbWUiOjE2MTYyOTMyMTcsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC51cy1lYXN0LTIuYW1hem9uYXdzLmNvbVwvdXMtZWFzdC0yX3NpMWNZSzJJTyIsImV4cCI6MTYxNjQ4ODE1OSwiaWF0IjoxNjE2NDg0NTU5LCJqdGkiOiI2YzE0ODkwNC04NzRkLTQ5NTYtYmUzOC04YTkxZDNhOWJmY2MiLCJjbGllbnRfaWQiOiIyNDFucGc0dW5uMTFwam1qbjJnMnBlOWI5ZSIsInVzZXJuYW1lIjoiY2VzYXIifQ.Kf8mnDa1HYkdAz53MblzulaAHK7wcLxe12Jto6ONECLcIaNIOnNSLXkD7oYVTlpWpiLk5JQNYK-KFuT5fyZ5hhzO6fs9G6MLz_PLquDSCNKlUHcwRqhDZuGi3sLPyMsK2Yv9aKfBh7NNaR5ZsyEvdfivXrmgRooSW4k09Fcl3bT-PTCigX8NXlBsMSFp6Ao9vahYfLh0aa0L0v050b2zt04TABawb-V9UwAki0DCRuoxPtN2IjcT7PiLL5a_I_PCmEOujrPsRGKM5bp7oUqwKG1WTvffbQ8qZANZGxV4TMJsb47UWZG8R-yo90GeN-986LIPPPFiZHc63rERp62S-g
//
//                                DecodedJWT jwt= jwtVerifier.verify(AWSMobileClient.getInstance().getTokens().getAccessToken().getTokenString());
//                                String username = jwt.getClaim("sub").asString();
//                                String email = jwt.getClaim("email").asString();
//                                String phone = jwt.getClaim("phone_number").asString();
//                                String[] groups = jwt.getClaim("cognito:groups").asArray(String.class);
//        Log.i("PLSWORK99:", "User ID: "+ username+"        User Pool: "+groups[0]);
//        //I/PLSWORK99:: User ID: 167e820c-9c32-4ede-a084-b065bad7a55f        User Pool: WMSAdmin
//                            /*
//                            {
//  "sub": "167e820c-9c32-4ede-a084-b065bad7a55f",
//  "cognito:groups": [
//    "WMSAdmin"
//  ],
//  "event_id": "b9c7fceb-85c4-4210-abc3-4ab74b8da488",
//  "token_use": "access",
//  "scope": "aws.cognito.signin.user.admin",
//  "auth_time": 1616223124,
//  "iss": "https://cognito-idp.us-east-2.amazonaws.com/us-east-2_si1cYK2IO",
//  "exp": 1616294282,
//  "iat": 1616290682,
//  "jti": "c98b4689-354c-4af5-8943-4ff9b795cc79",
//  "client_id": "241npg4unn11pjmjn2g2pe9b9e",
//  "username": "cesar"
//}
//clientID is the pool.
//
//                             */
//
//
//        } catch (Exception e){
//        Log.e("APPSYNC_ERROR", e.getLocalizedMessage());
//        e.getLocalizedMessage();
//        }