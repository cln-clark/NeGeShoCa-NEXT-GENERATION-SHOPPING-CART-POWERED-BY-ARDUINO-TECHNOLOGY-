package com.mycompany.posapplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseInitializer {
    
    private static boolean isInitialized = false;
    
     public static void initialize() throws IOException {
         
         if(!isInitialized){
            FileInputStream serviceAccount = new FileInputStream("C:\\Users\\Admin\\Documents\\NetBeansProjects\\POSApplication\\src\\negeshoca-firebase-adminsdk-95h4q-21fa0b7d0b.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://negeshoca-default-rtdb.asia-southeast1.firebasedatabase.app/") // Replace with your Firebase Database URL
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase initialized successfully!");
            isInitialized = true;         
        
         } else{
                String appName = FirebaseApp.getInstance().getName();
                System.out.println("Firebase is already initialized with: " + appName);
        }
          
    }
    
}
