# ImageVideoPicker lib
Media picker with image and video together.

## Open Activity
```                
val intent = Intent(this, MediaPickerActivity::class.java)
                intent.putExtra("limit",10)
                startActivityForResult(intent, REQ_CODE)
```

## On Activity Result
````kotlin 
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val selectionResult = data.getStringArrayListExtra("result")
                selectionResult.forEach {
                    try {
                        val uriFromPath = Uri.fromFile(File(it))
                        Log.d("MyApp", "Image URI : " + uriFromPath)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }
}
```

           
                