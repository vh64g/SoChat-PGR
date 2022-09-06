package com.example.social_network

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.util.*


class CustomArFragment : ArFragment(){
    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)
        val filter = CameraConfigFilter(session)

        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D

        filter.targetFps = EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30)
        filter.facingDirection = CameraConfig.FacingDirection.FRONT

        val cameraConfigList = session.getSupportedCameraConfigs(filter)
        session.cameraConfig = cameraConfigList[0]

        arSceneView.setupSession(session)
        return config
    }

    override fun getSessionFeatures(): Set<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val frameLayout = super.onCreateView(inflater, container, savedInstanceState) as FrameLayout?
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        return frameLayout
    }
}