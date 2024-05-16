package com.example.cab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    private Button CustomerLoginbtn;
    private TextView CustomerRegisterlink;
    private Button CustomerRegisterButton;
    private TextView CustomerStatus;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;
    private String onlineCustomerID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_login_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth=FirebaseAuth.getInstance();

        CustomerLoginbtn= findViewById(R.id.customer_login_btn);
        CustomerRegisterlink=findViewById(R.id.customer_register_btn);
        CustomerStatus=findViewById(R.id.customer_status);
        CustomerRegisterButton=findViewById(R.id.customer_register_button);
        EmailCustomer=findViewById(R.id.email_customer);
        PasswordCustomer=findViewById(R.id.password_customer);
        loadingBar=new ProgressDialog(this);


        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);


        CustomerRegisterlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerLoginbtn.setVisibility(View.INVISIBLE);
                CustomerRegisterlink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);
            }
        });
        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailCustomer.getText().toString();
                String password=PasswordCustomer.getText().toString();

                RegisterCustomer(email,password);
            }
        });

        CustomerLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailCustomer.getText().toString();
                String password=PasswordCustomer.getText().toString();

                SignInCustomer(email,password);

            }
        });



    }

    private void SignInCustomer(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write password", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Customer Login");
            loadingBar.setMessage("Please wait,while we are checking your credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {



                                Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapsActivity.class);
                                startActivity(customerIntent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login In Successful...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {
                                Toast.makeText(CustomerLoginRegisterActivity.this, " Login Unsuccessful,please try again...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }


    private void RegisterCustomer(String email, String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write password", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Customer Registration");
            loadingBar.setMessage("Please wait,while we are register your data...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                onlineCustomerID=mAuth.getCurrentUser().getUid();
                                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User").child("Customers").child(onlineCustomerID);


                                CustomerDatabaseRef.setValue(true);
                                   Intent driverInent=new Intent(CustomerLoginRegisterActivity.this,CustomersMapsActivity.class);
                                 startActivity(driverInent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Register Successful...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                            else {
                                Toast.makeText(CustomerLoginRegisterActivity.this, " Registration Unsuccessful,please try again...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}