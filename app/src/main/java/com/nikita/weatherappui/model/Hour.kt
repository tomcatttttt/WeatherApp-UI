import com.google.gson.annotations.SerializedName
import com.nikita.weatherappui.model.Condition


data class Hour(
    val time: String,
    @SerializedName("temp_c") val tempC: Double,
    val condition: Condition
)