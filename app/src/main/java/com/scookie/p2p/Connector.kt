package com.scookie.p2p

import android.app.Activity
import android.widget.Toast
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlin.text.Charsets.UTF_8

val myName = "Name-${java.util.Random().nextInt(1000)}"
val packageName = "com.scookie.p2p"

class Connector(
    private val act: Activity
): ConnectionLifecycleCallback() {

    private val STRATEGY = Strategy.P2P_STAR
    private lateinit var connectionsClient: ConnectionsClient
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    private var opponentName: String? = null
    private var opponentEndpointId: String? = null

    init {
        connectionsClient = Nearby.getConnectionsClient(act)
    }

    //// Connect

    fun startAdvertising() {
        connectionsClient.startAdvertising(
            myName,
            act.packageName,
            this,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        )

        Toast.makeText(act, "Started advertising", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
        connectionsClient.acceptConnection(endpointId, payloadCallback)
        opponentName = "Opponent\n(${info.endpointName})"
        Toast.makeText(act, "Found connection", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        Toast.makeText(act, "Connection accepted", Toast.LENGTH_SHORT).show()
        if (result.status.isSuccess) {
            connectionsClient.stopAdvertising()
            connectionsClient.stopDiscovery()
            opponentEndpointId = endpointId
        }
    }

    override fun onDisconnected(endpointId: String) {
        Toast.makeText(act, "Connection closed", Toast.LENGTH_SHORT).show()
    }

    //// Communication

    fun sendPayload(message: String) {
        opponentEndpointId ?: return
        connectionsClient.sendPayload(
            opponentEndpointId!!,
            Payload.fromBytes(message.toByteArray(UTF_8))
        )

        Toast.makeText(act, "Payload sent", Toast.LENGTH_SHORT).show()
    }

    private val payloadCallback: PayloadCallback = object : PayloadCallback() {

        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val payloadContent = payload.asBytes() ?: return
            Toast.makeText(act, String(payloadContent, Charsets.UTF_8), Toast.LENGTH_SHORT).show()
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Toast.makeText(act, endpointId, Toast.LENGTH_SHORT).show()
            }
        }

    }

    //// Discovery

    fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packageName, endpointDiscoveryCallback, options)

        Toast.makeText(act, "Started discover", Toast.LENGTH_SHORT).show()
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {

        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Toast.makeText(act, "Connection found", Toast.LENGTH_SHORT).show()
            connectionsClient.requestConnection(myName, endpointId, this@Connector)
        }

        override fun onEndpointLost(endpointId: String) {
        }
    }


    fun stop() {
        connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            stopAllEndpoints()
        }
    }

}