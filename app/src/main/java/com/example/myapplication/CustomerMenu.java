package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.UpdatePetMutation;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Regions;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.myapplication.Profile.Profile;
import com.example.myapplication.ShoppingCart.ShoppingCart;

import org.w3c.dom.Text;

import javax.annotation.Nonnull;

import type.UpdatePetInput;

public class CustomerMenu extends AppCompatActivity {
    private ListView employeeMenu_listView;
    private Toolbar customer_toolbar;
    View popupView;
    PopupWindow popupWindow;
    LayoutInflater reportInflater;

    // Mutation callback code
    private final GraphQLCall.Callback<UpdatePetMutation.Data> mutateCallback4 = new GraphQLCall.Callback<UpdatePetMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<UpdatePetMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CustomerMenu.this, "Updated admin view", Toast.LENGTH_LONG).show();
                    //DisplayItems.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform UpdateItemMutation", e);
                    Toast.makeText(CustomerMenu.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                    //DisplayItems.this.finish();
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.dbmain_menu, menu);
        if(currentUser.admin) {
            getMenuInflater().inflate(R.menu.dbmain_menu, menu);
            System.out.println("????????????????????????ADMIN MENU");
        }
        else if(currentUser.customer){
            getMenuInflater().inflate(R.menu.dbmain_menu_customer, menu);
            System.out.println("????????????????????????CUSTOMER MENU");

        }
        return true;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.item1:
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("id", 1);
                Intent intent = new Intent(getApplicationContext(), DisplayItems.class);
                intent.putExtras(dataBundle);
                startActivity(intent);
                return true;

            case R.id.item3:    // report 1 pie chart
                //Toast.makeText(myItems.this, "33333333333", Toast.LENGTH_LONG).show();
                currentUser.customer=false;
                currentUser.employee=false;
                moveToAuthentication();

                return true;

            case R.id.item6:    //Customer
                //setAdminView(currentUser.id,"customer");
                currentUser.customer=true;
                currentUser.employee=false;
                moveToAuthentication();
                return true;

            case R.id.item7: // Employee
                //Toast.makeText(myItems.this, "77777777777777777777777777777", Toast.LENGTH_LONG).show();
                //setAdminView(currentUser.id,"employee");
                currentUser.employee=true;
                currentUser.customer=false;
                moveToAuthentication();


                return true;

            case R.id.item4:
                //Toast.makeText(myItems.this, "Bye2", Toast.LENGTH_LONG).show();
                AWSMobileClient.getInstance().signOut();
                // initiate a credentials provider
                CognitoCachingCredentialsProvider provider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        "us-east-2_si1cYK2IO",
                        Regions.US_EAST_2);
                provider.clear();

                currentUser.loggingOut=true;

                Intent i = new Intent(getApplicationContext(), AuthenticationActivity.class);
                //intent.putExtras(dataBundle);
                startActivity(i);
                //IdentityManager.getDefaultIdentityManager().signOut();

                return true;

            case R.id.item5:
                //Settings
                Intent profile = new Intent(getApplicationContext(), Profile.class);
                startActivity(profile);

//https://aws.amazon.com/blogs/mobile/using-android-sdk-with-amazon-cognito-your-user-pools/

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays a Toast with the message.
     *
     * @param message Message to display
     */
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * onClick method when the user clicks the shopping cart in customer main menu
     *
     * @param view
     */
    public void getToSCR(View view) {
        if (view.getId() == R.id.customer_menu_shopping_cart) {
            Intent toEmployeeMainMenuIntent = new Intent(this, ShoppingCart.class);
            startActivity(toEmployeeMainMenuIntent);
        }
        else if(view.getId() == R.id.customer_menu_report) {
            displayToast("Report button clicked");
            // inflate the layout of the report window
            reportInflater = (LayoutInflater) getSystemService((LAYOUT_INFLATER_SERVICE));
            popupView = reportInflater.inflate(R.layout.report_window, (ViewGroup) findViewById(R.id.popupLayout));

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            boolean focusable = true;
            popupWindow = new PopupWindow(popupView, (int)(width*.9), (int)(height*.55), focusable);
            popupWindow.setAnimationStyle(R.style.Animation);

            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            Button reportBtn = (Button) popupView.findViewById(R.id.report_button);
            reportBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Send email", "");

                    TextView report = (TextView) popupView.findViewById(R.id.report_txt);

                    String subject = "Report from " + currentUser.name;
                    String issue = report.getText().toString();
                    String[] TO = {"teamBlueWMS@gmail.com"};
//        String[] CC = {};
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setData(Uri.parse("mailto:"));
                    emailIntent.setType("text/plain");

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
//        emailIntent.putExtra(Intent.EXTRA_CC, CC);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, issue);

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//                        finish();
                        Log.i("Finished sending email...", "");
                    } catch (android.content.ActivityNotFoundException ex) {
                        displayToast("There is no email client installed.");
                    }
                    popupWindow.dismiss();
                }
            });

            TextView exitReport = (TextView) popupView.findViewById(R.id.exitReport);
            exitReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                }
            });
        }
    }

    private void moveToAuthentication() {
        Intent i = new Intent(getApplicationContext(), AuthenticationActivity.class);
        startActivity(i);
    }

    //========================================================================================UPDATE
    private void setAdminView(String id, String view) {
        UpdatePetInput input = UpdatePetInput.builder().id(id).description(view).build();
        UpdatePetMutation updatePetMutation = UpdatePetMutation.builder().input(input).build();
        ClientFactory.appSyncClient().mutate(updatePetMutation).enqueue(mutateCallback4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu);
//        employeeMenu_listView = findViewById(R.id.employee_listView);
        customer_toolbar = findViewById(R.id.create_item_toolbar);
        setSupportActionBar(customer_toolbar);

        TextView intro = (TextView) findViewById(R.id.testtxt);
        intro.setText("Hello, " + currentUser.name + "!");


//        EmployeeMenuAdapter adapter = new EmployeeMenuAdapter(getApplicationContext(), employeeMenu, employeeMenuImage);
//        employeeMenu_listView.setAdapter(adapter);
//        employeeMenu_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                if (i == 0) {           // Manage Inventory
//                    Intent startManageInventory = new Intent(CustomerMenu.this, myItems.class);
//                    startActivity(startManageInventory);
//
//                } else if (i == 1) {   // Order List
////                    Intent startOrderList = new Intent(EmployeeMenu.this, OrderList.class);
////                    startActivity(startOrderList);
//
//                } else if (i == 2) {   // Shift Management
//                    displayToast("Shift Management Clicked! ");
//
//                } else if (i == 3) {   // Suppliers
////                    Intent startAddSupplier = new Intent(EmployeeMenu.this, add_supplier.class);
////                    startActivity(startAddSupplier);
//
//                } else if (i == 4) {   // Customers
//                    Intent startCustomer = new Intent(CustomerMenu.this, DisplayCustomer.class);
//                    startActivity(startCustomer);
//
//                }
//            }
//        });

    }
}
