package com.mapbox.navigation.core

import com.google.common.io.ByteStreams
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.mapbox.navigation.core.telemetry.events.*
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import java.util.zip.GZIPInputStream

class SchemaTest {
    @Before
    @Throws(Exception::class)
    fun downloadSchema() {
        unpackSchemas()
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationArriveEventSize() {
        val schema = grabSchema(NAVIGATION_ARRIVE)
        val fields = grabClassFields(NavigationArriveEvent::class.java)
        Assert.assertEquals(schema!!.size().toLong(), fields.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationArriveEventFields() {
        val schema = grabSchema(NAVIGATION_ARRIVE)
        val fields = grabClassFields(NavigationArriveEvent::class.java)
        schemaContainsFields(schema, fields)
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationCancelEventSize() {
        val schema = grabSchema(NAVIGATION_CANCEL)
        val fields = grabClassFields(NavigationCancelEvent::class.java)
        Assert.assertEquals(schema!!.size().toLong(), fields.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationCancelEventFields() {
        val schema = grabSchema(NAVIGATION_CANCEL)
        val fields = grabClassFields(NavigationCancelEvent::class.java)
        schemaContainsFields(schema, fields)
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationDepartEventSize() {
        val schema = grabSchema(NAVIGATION_DEPART)
        val fields = grabClassFields(NavigationDepartEvent::class.java)
        Assert.assertEquals(schema!!.size().toLong(), fields.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationDepartEventFields() {
        val schema = grabSchema(NAVIGATION_DEPART)
        val fields = grabClassFields(NavigationDepartEvent::class.java)
        schemaContainsFields(schema, fields)
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationFeedbackEventSize() {
        val schema = grabSchema(NAVIGATION_FEEDBACK)
        val fields = grabClassFields(NavigationFeedbackEvent::class.java)
        Assert.assertEquals(schema!!.size().toLong(), fields.size.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationFeedbackEventFields() {
        val schema = grabSchema(NAVIGATION_FEEDBACK)
        val fields = grabClassFields(NavigationFeedbackEvent::class.java)
        schemaContainsFields(schema, fields)
    }

    @Test
    @Throws(Exception::class)
    fun checkNavigationRerouteEventSize() {
        val schema = grabSchema(NAVIGATION_REROUTE)
        val fields = grabClassFields(NavigationRerouteEvent::class.java)
        Assert.assertEquals(schema!!.size().toLong(), fields.size.toLong())
    }

    @Test
    fun checkNavigationRerouteEventFields() {
        val schema = grabSchema(NAVIGATION_REROUTE)
        val fields = grabClassFields(NavigationRerouteEvent::class.java)
        schemaContainsFields(schema, fields)
    }

    private fun schemaContainsFields(schema: JsonObject?, fields: List<Field>) {
        var distanceRemainingCount = 0
        var durationRemainingCount = 0
        for (i in fields.indices) {
            val thisField = fields[i].toString()
            val fieldArray = thisField.split(" ").toTypedArray()
            val typeArray = fieldArray[fieldArray.size - 2].split("\\.").toTypedArray()
            val type = typeArray[typeArray.size - 1]
            val nameArray = fieldArray[fieldArray.size - 1].split("\\.").toTypedArray()
            var field = nameArray[nameArray.size - 1]
            val serializedName = fields[i].getAnnotation(SerializedName::class.java)
            if (serializedName != null) {
                field = serializedName.value
            }
            if (field.equals("durationRemaining", ignoreCase = true)) {
                durationRemainingCount++
                if (durationRemainingCount > 1) {
                    field = "step$field"
                }
            }
            if (field.equals("distanceRemaining", ignoreCase = true)) {
                distanceRemainingCount++
                if (distanceRemainingCount > 1) {
                    field = "step$field"
                }
            }
            val thisSchema = findSchema(schema, field)
            Assert.assertNotNull(field, thisSchema)
            if (thisSchema.has("type")) {
                typesMatch(thisSchema, type)
            }
        }
    }

    private fun findSchema(schema: JsonObject?, field: String): JsonObject {
        return schema!!.getAsJsonObject(field)
    }

    private fun typesMatch(schema: JsonObject, t: String) {
        var type = t
        if (type.equals("int", ignoreCase = true) || type.equals("integer", ignoreCase = true)
                || type.equals("double", ignoreCase = true) || type.equals("float", ignoreCase = true)) {
            type = "number"
        }
        if (type.contains("[]")) {
            type = "array"
        }
        val typeClass: Class<out JsonElement> = schema["type"].javaClass
        val jsonElement = JsonParser().parse(type.toLowerCase())
        if (typeClass == JsonPrimitive::class.java) {
            val typePrimitive = schema["type"]
            Assert.assertTrue(typePrimitive == jsonElement)
        } else {
            val arrayOfTypes = schema.getAsJsonArray("type")
            Assert.assertTrue(arrayOfTypes.contains(jsonElement))
        }
    }

    private fun grabSchema(eventName: String): JsonObject? {
        for (thisSchema in schemaArray!!) {
            val name = thisSchema["name"].asString
            if (name.equals(eventName, ignoreCase = true)) {
                val gson = Gson()
                var schemaString = gson.toJson(thisSchema["properties"])
                var schema = gson.fromJson(thisSchema["properties"], JsonObject::class.java)
                if (schema.has("step")) {
                    val stepJson = schema["step"].asJsonObject
                    val stepProperties = stepJson["properties"].asJsonObject
                    val stepPropertiesJson = gson.toJson(stepProperties)
                    schemaString = generateStepSchemaString(stepPropertiesJson, schemaString)
                    schema = gson.fromJson(schemaString, JsonObject::class.java)
                    schema.remove("step")
                }
                schema.remove("userAgent")
                schema.remove("received")
                schema.remove("token")
                schema.remove("authorization")
                schema.remove("owner")
                schema.remove("locationAuthorization")
                schema.remove("locationEnabled")
                //temporary need to work out a solution to include this data
                schema.remove("platform")
                return schema
            }
        }
        return null
    }

    private fun grabClassFields(aClass: Class<*>): List<Field> {
        val fields: MutableList<Field> = ArrayList()
        val allFields = aClass.declaredFields
        for (field in allFields) {
            if (field.type == NavigationStepData::class.java) {
                val dataFields = field.type.declaredFields
                for (dataField in dataFields) {
                    if (Modifier.isPrivate(dataField.modifiers) && !Modifier.isStatic(dataField.modifiers)) {
                        fields.add(dataField)
                    }
                }
            } else if (Modifier.isPrivate(field.modifiers) && !Modifier.isStatic(field.modifiers)) {
                fields.add(field)
            }
        }
        val superFields = aClass.superclass.declaredFields
        for (field in superFields) {
            if (Modifier.isPrivate(field.modifiers) && !Modifier.isStatic(field.modifiers)) {
                fields.add(field)
            }
        }
        return fields
    }

    private fun removeField(fields: MutableList<Field>, fieldName: String): List<Field> {
        for (field in ArrayList(fields)) {
            val thisField = field.toString()
            val fieldArray = thisField.split("\\.").toTypedArray()
            val simpleField = fieldArray[fieldArray.size - 1]
            if (simpleField.equals(fieldName, ignoreCase = true)) {
                fields.remove(field)
            }
        }
        return fields
    }

    private fun generateStepSchemaString(step: String, schemaString: String): String {
        var stepJson = step
        var schemaString = schemaString
        stepJson = stepJson.replace("\"distanceRemaining\"", "\"stepdistanceRemaining\"")
        stepJson = stepJson.replace("durationRemaining", "stepdurationRemaining")
        stepJson = stepJson.replaceFirst("\\{".toRegex(), ",")
        schemaString = schemaString.replace("}$".toRegex(), "")
        schemaString = schemaString + stepJson
        return schemaString
    }

    companion object {
        private const val NAVIGATION_ARRIVE = "navigation.arrive"
        private const val NAVIGATION_CANCEL = "navigation.cancel"
        private const val NAVIGATION_DEPART = "navigation.depart"
        private const val NAVIGATION_FASTER_ROUTE = "navigation.fasterRoute"
        private const val NAVIGATION_FEEDBACK = "navigation.feedback"
        private const val NAVIGATION_REROUTE = "navigation.reroute"
        private var schemaArray: ArrayList<JsonObject>? = null

        @BeforeClass
        @Throws(Exception::class)
        fun downloadSchema() {
            unpackSchemas()
        }

        @get:Throws(IOException::class)
        private val fileBytes: ByteArrayInputStream
            get() {
                val inputStream = SchemaTest::class.java.classLoader.getResourceAsStream("mobile-event-schemas.jsonl.gz")
                val byteOut = ByteStreams.toByteArray(inputStream)
                return ByteArrayInputStream(byteOut)
            }

        @Throws(IOException::class)
        private fun unpackSchemas() {
            val bais = fileBytes
            val gzis = GZIPInputStream(bais)
            val reader = InputStreamReader(gzis)
            val `in` = BufferedReader(reader)
            schemaArray = ArrayList()
            val gson = Gson()
            var readed: String?
            while (`in`.readLine().also { readed = it } != null) {
                val schema = gson.fromJson(readed, JsonObject::class.java)
                schemaArray!!.add(schema)
            }
        }
    }
}