package com.example.social_network

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Frame
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode
import java.util.*
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {

    private var hasCamera: Boolean = false
    private var modelRenderable: ModelRenderable? = null
    private var texture: Texture? = null
    private var isAdded: Boolean = false
    private var usingAr: Boolean? = null

    var asset = Array(2){R.raw.fox_face; R.drawable.fox_face_mesh_texture}

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
        hasCamera = hasCameraHardware(this)
        if (hasCamera){
            checkAr()
            if(usingAr!!){manageArFragment()}
        } else {
            Toast.makeText(this, "No camera detected", Toast.LENGTH_LONG).show()
            val intent: Intent = Intent(applicationContext, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
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
    fun loadLens(context: Context, asset: Int, texture: Int){
        ModelRenderable.builder()
            .setSource(this, asset)
            .build()
            .thenAccept(Consumer { renderable: ModelRenderable ->
                this.modelRenderable = renderable
            })
        Texture.builder()
            .setSource(this, texture)
            .build()
            .thenAccept(Consumer { texture: Texture ->
                this.texture = texture
            })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun manageArFragment() {
        var customArFragment: CustomArFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as CustomArFragment
        loadLens(this, R.raw.canonical_face_mesh, R.drawable.canonical_face_texture)
        customArFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        updateListener(customArFragment)
    }

    private fun updateListener(customArFragment: CustomArFragment){
        customArFragment.arSceneView.scene.addOnUpdateListener { frameTime: FrameTime ->
            if(modelRenderable == null || texture == null) {return@addOnUpdateListener}
            val frame: Frame? = customArFragment.arSceneView.arFrame
            val augmentedFaces = frame?.getUpdatedTrackables(AugmentedFace::class.java)
            if (augmentedFaces != null) { for (augmentedFace: AugmentedFace in augmentedFaces) { if (!isAdded) {
                renderAr(customArFragment, augmentedFace)
                isAdded = true
            } } }
        }
    }

    private fun renderAr(customArFragment: CustomArFragment, augmentedFace: AugmentedFace){
        val augmentedFaceNode = AugmentedFaceNode(augmentedFace)
        augmentedFaceNode.setParent(customArFragment.arSceneView.scene)
        augmentedFaceNode.faceRegionsRenderable = modelRenderable
        augmentedFaceNode.faceMeshTexture = texture
    }
}