# ShareScreen-Firebase-Android

For Android, by [HOMINHTUNG](https://github.com/HOMINHTUNG)

## Description

Stream video screen device with Firebase, native player on Android.

Here I have two app to test:

* ShareScreen: People share your screen device

<img src="https://i.imgur.com/BwgEYvZ.png">

<img src="https://i.imgur.com/e1OyL9b.png">

* SeenScreen: People want to see the screen

<img src="https://i.imgur.com/Z1iNLhk.jpg">

<img src="https://i.imgur.com/GK4WTDC.jpg">

<img src="https://i.imgur.com/Dc9PC6m.jpg">


### Android specifics

* Uses ImageView, ImageReader, MediaProjection.
* Server Firebase: Firebase Message, Database Firebase.
* Tested on Android 5.0+

## Usage

* ShareScreen: I try to capture the screen and encode base64 image (convert byte[] to string) and String send up server Firebase.

```java

  private void sendBitmapBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            encoder = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d(TAG, "bitmap: " + encoder);
        } catch (Exception e) {
            e.printStackTrace();
        }

//      Load data
        uploadFireBase();
    }

    //  check load date lên firebase
    public void uploadFireBase() {
        try {
            myRef.setValue(encoder, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.d(TAG, "upload ERROR: " + databaseError);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

```

* SeenScreen: I create VideoView and load Byte[] after get String from Firebase and decode base64 string (convert string to byte[]).

```java

  //Load image record in database realtime firebase
    private void dataChangeServer(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                try{
                    mBitmapBase64 = dataSnapshot.getValue(String.class);
                    imageView.setImageBitmap(base64ToBitmap(mBitmapBase64));

                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Khong doc duoc du lieu.", error.toException());
            }
        });
    }

    private Bitmap base64ToBitmap (String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

```
### Notes

You need to change something to use it:

* Connect server your Firebase
* public static String FB_KEY_API
* public static String FB_TOKEN_ADMIN
* public static String FB_TOKEN_DRIVER
