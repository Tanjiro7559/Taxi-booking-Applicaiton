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

public class DriverLoginRegisterActivity extends AppCompatActivity {
    private Button DriverLoginbtn;
    private TextView DriverRegisterlink;
    private Button DriverRegisterButton;
    private TextView DriverStatus;
    private EditText EmailDriver;
    private EditText PasswordDriver;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference DriverDatabaseRef;
    private String onlineDriverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_login_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth=FirebaseAuth.getInstance();


        DriverLoginbtn= findViewById(R.id.driver_login_btn);
        DriverRegisterlink=findViewById(R.id.driver_register_btn);
        DriverStatus=findViewById(R.id.driver_status);
        DriverRegisterButton=findViewById(R.id.driver_Register_button);
        EmailDriver=findViewById(R.id.email_driver);
        PasswordDriver=findViewById(R.id.password_driver);
        loadingBar=new ProgressDialog(this);


        DriverRegisterButton.setVisibility(View.INVISIBLE);
        DriverRegisterButton.setEnabled(false);


        DriverRegisterlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DriverLoginbtn.setVisibility(View.INVISIBLE);
                DriverRegisterlink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Register Driver");

                DriverRegisterButton.setVisibility(View.VISIBLE);
                DriverRegisterButton.setEnabled(true);
            }
        });

        DriverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailDriver.getText().toString();
                String password=PasswordDriver.getText().toString();

                RegisterDriver(email,password);
            }
        });
        DriverLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailDriver.getText().toString();
                String password=PasswordDriver.getText().toString();

                SignInDriver(email,password);

            }
        });
    }

    private void SignInDriver(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write password", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Driver Login");
            loadingBar.setMessage("Please wait,while we are checking your credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {

                                Intent driverIntent = new Intent(DriverLoginRegisterActivity.this,DriversMapActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Login in Successful...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                            else {
                                Toast.makeText(DriverLoginRegisterActivity.this, " Login Unsuccessful,please try again...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    private void RegisterDriver(String email, String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write password", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Driver Registration");
            loadingBar.setMessage("Please wait,while we are register your data...");
            loadingBar.show();

                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    onlineDriverID=mAuth.getCurrentUser().getUid();
                                    DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User").child("Driver").child(onlineDriverID);

                                    DriverDatabaseRef.setValue(true);
                                    Intent driverInent=new Intent(DriverLoginRegisterActivity.this,DriversMapActivity.class);
                                    startActivity(driverInent);

                                    Toast.makeText(DriverLoginRegisterActivity.this, "Driver Register Successful...", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                                else {
                                    Toast.makeText(DriverLoginRegisterActivity.this, " Registration Unsuccessful,please try again...", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
        }
    }
}