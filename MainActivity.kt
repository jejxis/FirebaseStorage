package net.flow9.thisiskotlin.firebasestorage

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PerformanceHintManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import net.flow9.thisiskotlin.firebasestorage.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val storage = Firebase.storage("gs://android-kotlin-firebase-debb2.appspot.com")
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnUpload.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        binding.btnDownload.setOnClickListener {
            downloadImage("images/temp_1654062207433.jpeg")
        }
    }

    val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            galleryLauncher.launch("image/*")
        }else{
            Toast.makeText(baseContext, "외부 저장소 읽기 권한을 승인해야 사용할 수 있습니다", Toast.LENGTH_LONG).show()
        }
    }

    val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){//이미지 갤러리 런처
        uri -> uploadImage(uri)
    }

    fun downloadImage(path: String){
        //스토리지 레퍼런스 연결하고 이미지 uri 가져오기
        storage.getReference(path).downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(binding.imageView)
        }.addOnFailureListener {
            Log.e("스토리지", "다운로드 에러=>${it.message}")
        }
    }

    fun uploadImage(uri: Uri){
        //경로+사용자아이디+밀리초 로 파일 주소 만들기
        val fulPath = makeFilePath("images", "temp", uri)
        //스토리지에 저장할 경로 설정
        val imageRef = storage.getReference(fulPath)
        //업로드 태스크 생성
        val uploadTask = imageRef.putFile(uri)

        //업로드 실행 및 결과 확인
        uploadTask.addOnFailureListener{
            Log.d("스토리지", "실패=>${it.message}")
        }.addOnSuccessListener { taskSnapshot ->
            Log.d("스토리지", "성공 주소=>${fulPath}")//경로를 db에 저장하고 사용
        }
    }

    fun makeFilePath(path: String, userId:String, uri:Uri): String{
        val mimeType = contentResolver.getType(uri)?:"/none"//ex: images/jpeg
        val ext = mimeType.split("/")[1]//ex: jpeg
        val timeSuffix = System.currentTimeMillis()//ex:1232131241312
        val filename = "${path}/${userId}_${timeSuffix}.${ext}"//완성..ex:경로/사용자ID_1232131241312.jpeg
        return filename
    }
}