package com.example.social_network

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.*
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Sun
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode
import java.io.*
import java.util.function.Consumer


class MainActivity : AppCompatActivity() {
    // Google ARCore
    private var modelRenderable: ModelRenderable? = null
    private var texture: Texture? = null
    private var isAdded: Boolean = false
    private var usingAr: Boolean? = false
    private var faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()
    private var currentFrame: Frame? = null
    private var arInUse: Boolean = false
    private var delMask: Boolean = false

    //Camera
    private var hasCamera: Boolean = false
    private var mWidth = Resources.getSystem().getDisplayMetrics().widthPixels
    private var mHeight = Resources.getSystem().getDisplayMetrics().heightPixels
    private var capturePicture = false

    //Buttons
    private var arBtnLeft: Button? = null
    private var arBtnRight: Button? = null

    var asset = Array(2) { R.raw.fox_face; R.drawable.fox_face_mesh_texture }

    private companion object {
        const val TAG = "MainActivity"
    }

    private fun hasCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Socialnetwork)
        setContentView(R.layout.activity_main)
        arBtnLeft = this.findViewById(R.id.btnLeftAr)
        arBtnRight = this.findViewById(R.id.btnRightAr)
        hasCamera = hasCameraHardware(this)
        if (hasCamera) {
            checkAr()
            getPermissions()
            if (usingAr!!) { manageArFragment() } else { manageCamera() }
        } else {
            Toast.makeText(this, "No camera detected", Toast.LENGTH_LONG).show()
            val intent: Intent = Intent(applicationContext, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getPermissions() {
        requestPermissions(arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ), 1)
    }

    private fun manageCamera() {

    }

    private fun checkAr() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({ checkAr() }, 200)
        }
        usingAr = availability.isSupported
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadLens(context: Context, asset: Int?, texture: Int?) {
        // Load the model.
        if (asset == null || texture == null) {
            this.modelRenderable = null
            this.texture = null
        } else {
            ModelRenderable.builder()
                .setSource(this, asset)
                .build()
                .thenAccept(Consumer { renderable: ModelRenderable ->
                    this.modelRenderable = renderable
                    //this.modelRenderable?.isShadowCaster = true
                    //this.modelRenderable?.isShadowReceiver = true
                })
                .exceptionally { throwable: Throwable ->
                    Toast.makeText(context, "Unable to load renderable", Toast.LENGTH_LONG).show()
                    null
                }
            // Load the texture.
            Texture.builder()
                .setSource(this, texture)
                .build()
                .thenAccept(Consumer { texture: Texture ->
                    this.texture = texture
                })
                .exceptionally { throwable: Throwable ->
                    Toast.makeText(context, "Unable to load texture", Toast.LENGTH_LONG).show()
                    null
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun manageArFragment() {
        val customArFragment: CustomArFragment =
            supportFragmentManager.findFragmentById(R.id.arFragment) as CustomArFragment
        loadLens(this, null, null)
        customArFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        updateListener(customArFragment)
    }

    private fun updateListener(customArFragment: CustomArFragment) {
        customArFragment.arSceneView.scene.addOnUpdateListener { frameTime: FrameTime ->
            val frame: Frame? = customArFragment.arSceneView.arFrame
            this.currentFrame = frame
            val augmentedFaces = frame?.getUpdatedTrackables(AugmentedFace::class.java)
            if (augmentedFaces != null) {
                for (augmentedFace: AugmentedFace in augmentedFaces) {
                    if (!isAdded) {if (modelRenderable != null || texture != null) {renderAr(customArFragment, augmentedFace)}}
                    else{
                        if (delMask){
                            clearAr(customArFragment, augmentedFace)
                            delMask = false
                        }
                    }
                }
            }
            if (capturePicture) {capturePicture = false;savePicture()}
        }
    }

    private fun clearAr(customArFragment: CustomArFragment, augmentedFace: AugmentedFace) {
        try {
            customArFragment.arSceneView.scene.removeChild(faceNodeMap[augmentedFace])
            isAdded = false
        } catch (e: Exception) {}
    }

    private fun renderAr(customArFragment: CustomArFragment, augmentedFace: AugmentedFace) {
        val augmentedFaceMode = AugmentedFaceNode(augmentedFace)
        augmentedFaceMode.setParent(customArFragment.arSceneView.scene)
        augmentedFaceMode.faceRegionsRenderable = modelRenderable
        augmentedFaceMode.faceMeshTexture = texture
        faceNodeMap.put(augmentedFace, augmentedFaceMode)
        isAdded = true
        val iterator: MutableIterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> =
            faceNodeMap.entries.iterator()
        val (face, node) = iterator.next()
        while (face.trackingState == TrackingState.STOPPED) {
            node.setParent(null)
            iterator.remove()
        }
    }

    fun takePictureAr(view: View) {
        capturePicture = true;
    }

    @Throws(IOException::class)
    fun savePicture() {

        mWidth = Resources.getSystem().displayMetrics.widthPixels
        mHeight = Resources.getSystem().displayMetrics.heightPixels

        // Create a file in the Pictures/HelloAR album.
        val out = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).toString() + "/SoChat", "Img" +
                    java.lang.Long.toHexString(System.currentTimeMillis()) + ".png"
        )
        // Make sure the directory exists
        if (!out.parentFile?.exists()!!) {out.parentFile?.mkdirs()}

        val image:Image? = currentFrame?.acquireCameraImage()
        val imageFormat: Int = image!!.format
        if (imageFormat == ImageFormat.YUV_420_888) {
            // Create a bitmap.
            val bmp_res: ByteArray? = NV21toJPEG(YUV_420_888toNV21(image), image.width, image.height)
            val bmp:Bitmap? = BitmapFactory.decodeByteArray(bmp_res, 0, bmp_res!!.size, null)
            var mat:Matrix = Matrix()
            mat.postRotate(-90f)
            val rotatedBmp: Bitmap = Bitmap.createBitmap(bmp!!, 0, 0, bmp.width, bmp.height, mat, true)
            // Write it to disk.
            val fos = FileOutputStream(out)
            rotatedBmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show()
        }

    }

    private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
        return out.toByteArray()
    }

    private fun YUV_420_888toNV21(image: Image): ByteArray {
        val nv21: ByteArray
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        return nv21
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun toggleFaceAr(view: View) {
        if (arInUse){
            arInUse = false
            loadLens(this, null, null)
            delMask = true
            arBtnLeft!!.visibility = View.GONE
            arBtnRight!!.visibility = View.GONE
        } else {
            arInUse = true
            loadLens(this, R.raw.fox_face, R.drawable.fox_face_mesh_texture)
            arBtnLeft!!.visibility = View.VISIBLE
            arBtnRight!!.visibility = View.VISIBLE
        }
    }
    fun switchLeftAr(view: View) {}
    fun switchRightAr(view: View) {}
}