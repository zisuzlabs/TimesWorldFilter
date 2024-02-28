package com.example.timesworldfilter

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timesworldfilter.ui.theme.TimesWorldFilterTheme
import com.google.gson.Gson



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Parse the JSON string
        val gson = Gson()
        val dataList = gson.fromJson(jsonString, Map::class.java)["data"] as List<*>
        // Convert each item in the list to a DataItem
        // Convert each item in the list to a DataItem
        val items = dataList.map { item ->
            val map = item as Map<*, *>
            DataItem(
                name = map["name"] as String,
                slug = map["slug"] as String,
                taxonomies = (map["taxonomies"] as List<*>).map { it as Map<*, *> }
                    //.map { Taxonomy(name = it["name"] as String) }
                    .map {
                        Taxonomy(
                            id = it["id"] as? Double,
                            Guid = it["Guid"] as? String,
                            slug = it["slug"] as? String,
                            name = it["name"] as? String,
                            city = it["city"] as? String,
                            locations = (it["locations"] as List<*>?)?.map { it as Map<*, *> }?.map {
                                Location(
                                    id = it["id"] as Double,
                                    Guid = it["Guid"] as String,
                                    slug = it["slug"] as String,
                                    name = it["name"] as String
                                )
                            }
                        )
                    }
            )
        }

        setContent {
            TimesWorldFilterTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DataScreen(dataList = items)
                }
            }
        }
    }
}

@Composable
fun DataScreen(dataList: List<DataItem>) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Single state for overall selection
        val selectedTaxonomy = remember { mutableStateOf<Taxonomy?>(null) }

        // Top section displaying selected item or default message
        SelectionChip(selectedTaxonomy = selectedTaxonomy)

        // ExpandableItem for each data item with individual selection
        dataList.forEach { dataItem ->
            ExpandableItem(
                title = dataItem.name,
                content = {
                    TaxonomySelection(dataItem.taxonomies, selectedTaxonomy)
                }
            )
        }
    }
}

// SelectionChip to display overall selected item
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SelectionChip(selectedTaxonomy: MutableState<Taxonomy?>) {
    // Using a derived state variable to update the list based on changes
    val selectedTaxonomyNames by derivedStateOf {
        if (selectedTaxonomy.value != null) {
            listOf(selectedTaxonomy.value!!.name!!) // Assuming name is not null
        } else {
            emptyList()
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        selectedTaxonomyNames.forEach { name ->
            AssistChip(
                onClick = { Log.d("Assist chip", "dismiss chip") },
                label = { Text(text = name) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Localized description",
                        Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
        }
    }
}

// TaxonomySelection for individual item selection
@Composable
fun TaxonomySelection(taxonomies: List<Taxonomy>, selected: MutableState<Taxonomy?>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Radio buttons for selecting single item per data item
        taxonomies.forEach { taxonomy ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(
                    selected = selected.value == taxonomy,
                    onClick = { selected.value = taxonomy }
                )
                Text(text = taxonomy.name ?: "(No name)")
            }
        }
    }
}


@Composable
fun ExpandableItem(
    title: String,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title)
            Icon(icon, contentDescription = "Expand/Collapse")
        }

        if (expanded) {
            content()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}


data class DataItem(
    val name: String,
    val slug: String,
    val taxonomies: List<Taxonomy>
)

data class Taxonomy(
    val id: Double? = null,
    val Guid: String? = null,
    val slug: String? = null,
    val name: String? = null,
    val city: String? = null,
    val locations: List<Location>? = null
)

data class Location(
    val id: Double,
    val Guid: String,
    val slug: String,
    val name: String
)

data class FilterGroup(val title: String, val options: List<String>)

val jsonString = """
        {
    "data": [
        {
            "name": "Cuisines",
            "slug": "cuisine",
            "taxonomies": [
                {
                    "id": 80,
                    "Guid": "4350EBAA-7189-4173-9180-BD95460401C7",
                    "slug": "international",
                    "name": "International"
                },
                {
                    "id": 82,
                    "Guid": "A7104031-6E6D-452F-A0AF-34E3271E4DA4",
                    "slug": "italian",
                    "name": "Italian"
                },
                {
                    "id": 70,
                    "Guid": "575B7732-AA5A-45F8-9E50-FA350FD92C8C",
                    "slug": "contemporary-1",
                    "name": "Contemporary"
                },
                {
                    "id": 79,
                    "Guid": "53BC06AD-65CC-4348-B087-2BB450667EBE",
                    "slug": "indian",
                    "name": "Indian"
                },
                {
                    "id": 642,
                    "Guid": "E2B0D8CA-0BA7-4B94-9485-7A2D35A0B12A",
                    "slug": "englishbritish",
                    "name": "English/British"
                },
                {
                    "id": 640,
                    "Guid": "A665F9D4-2557-4FD9-BBD2-AA7D2071C308",
                    "slug": "east-european",
                    "name": "East European"
                },
                {
                    "id": 57,
                    "Guid": "AD4A6C06-5443-4ACB-8033-EE4189E10F2A",
                    "slug": "asian",
                    "name": "Asian"
                },
                {
                    "id": 636,
                    "Guid": "4CFDB030-F6D8-4BC3-ADD7-F1D76D47E94E",
                    "slug": "australian",
                    "name": "Australian"
                },
                {
                    "id": 637,
                    "Guid": "A233D7B3-13CF-4223-989B-550405BB5EAE",
                    "slug": "austrian",
                    "name": "Austrian"
                },
                {
                    "id": 638,
                    "Guid": "4BCF0EED-7533-4231-9919-74FBA246DFEC",
                    "slug": "azerbaijan",
                    "name": "Azerbaijan"
                },
                {
                    "id": 639,
                    "Guid": "69FAABD6-6ADC-4ADD-94A6-C55F6EC8BDF9",
                    "slug": "brazilian",
                    "name": "Brazilian"
                },
                {
                    "id": 56,
                    "Guid": "A2731977-9D50-44F2-B841-98A6D745EE1E",
                    "slug": "american",
                    "name": "American"
                },
                {
                    "id": 74,
                    "Guid": "9DBBE3F5-A38A-49E3-B319-72D0585B04D2",
                    "slug": "french",
                    "name": "French"
                },
                {
                    "id": 83,
                    "Guid": "6E0DF8F0-0F51-49FB-A452-5AABDD19C621",
                    "slug": "japanese",
                    "name": "Japanese"
                },
                {
                    "id": 84,
                    "Guid": "0E1EF74C-0AF1-4180-9D70-278F8F97701B",
                    "slug": "latin-american",
                    "name": "Latin American"
                },
                {
                    "id": 88,
                    "Guid": "AA98FA08-C29A-49C9-BE25-E6D249E64C55",
                    "slug": "mexican",
                    "name": "Mexican"
                },
                {
                    "id": 103,
                    "Guid": "8A7B0E8E-5A6A-46F4-9D4F-ECC019A4BB89",
                    "slug": "steak",
                    "name": "Steak"
                },
                {
                    "id": 120,
                    "Guid": "994F9831-3EA8-4F93-BEAE-40B7150CC2DC",
                    "slug": "vegetarian",
                    "name": "Vegetarian"
                },
                {
                    "id": 131,
                    "Guid": "BA7FADCD-5B0E-44F8-8587-057D3AA7CB80",
                    "slug": "continental",
                    "name": "Continental"
                }
            ]
        },
        {
            "name": "Suitable Diets",
            "slug": "suitable-diet",
            "taxonomies": [
                {
                    "id": 504,
                    "Guid": "245DF0F7-CCFC-4ADD-9D31-7C1D2AD51731",
                    "slug": "gluten-free",
                    "name": "Gluten-Free"
                },
                {
                    "id": 517,
                    "Guid": "5E73CD24-3B58-4258-8DC0-6C1585616BE9",
                    "slug": "vegetarian-sd",
                    "name": "Vegetarian"
                },
                {
                    "id": 502,
                    "Guid": "042C5252-7126-48BB-B457-AD4ABE31D587",
                    "slug": "vegan-sd",
                    "name": "Vegan"
                },
                {
                    "id": 503,
                    "Guid": "B6B47FC6-3470-41BB-9AC8-47EBEABC4DEB",
                    "slug": "dairy-free",
                    "name": "Dairy-Free"
                },
                {
                    "id": 506,
                    "Guid": "4299B3C8-196D-437C-B3C9-06454EFD3E53",
                    "slug": "low-carb",
                    "name": "Low-Carb"
                },
                {
                    "id": 509,
                    "Guid": "90D77DE6-9600-4ACF-8317-3BEE252A2255",
                    "slug": "pescatarian",
                    "name": "Pescatarian"
                },
                {
                    "id": 510,
                    "Guid": "6E16198B-E072-4442-AC92-5CB880D4FC00",
                    "slug": "lactose-free",
                    "name": "Lactose-Free"
                },
                {
                    "id": 513,
                    "Guid": "AF13A334-68F0-486F-A1A0-866E52BDBF55",
                    "slug": "halal",
                    "name": "Halal"
                },
                {
                    "id": 518,
                    "Guid": "88310461-97A5-4D0A-9861-A16F9DA2E558",
                    "slug": "low-fat",
                    "name": "Low-Fat"
                },
                {
                    "id": 520,
                    "Guid": "BB0013C6-84E4-4257-A422-892C4D9A668F",
                    "slug": "low-sugar",
                    "name": "Low-Sugar"
                }
            ]
        },
        {
            "name": "Experiences",
            "slug": "experience",
            "taxonomies": [
                {
                    "id": 609,
                    "Guid": "C78114C8-50D9-41C5-9760-B7E47E5E0C05",
                    "slug": "casual-dining",
                    "name": "Casual Dining"
                },
                {
                    "id": 351,
                    "Guid": "BE97E925-D2EB-4CC8-B3BC-77F417C5050C",
                    "slug": "live-entertainment",
                    "name": "Live Entertainment"
                },
                {
                    "id": 359,
                    "Guid": "63440A4E-92E7-451E-B5FA-37187785DE0E",
                    "slug": "set-menu",
                    "name": "Set Menu"
                },
                {
                    "id": 603,
                    "Guid": "D676B818-827A-4C40-9E2D-0FA5E5BF7637",
                    "slug": "al-fresco-dining",
                    "name": "Al Fresco Dining"
                },
                {
                    "id": 615,
                    "Guid": "70914569-2136-4E41-8B91-46959F298C49",
                    "slug": "family-style",
                    "name": "Family Style"
                },
                {
                    "id": 633,
                    "Guid": "4B1BDD5A-DB1D-48E8-8694-444805B18F37",
                    "slug": "sports-bar-1",
                    "name": "Sports Bar"
                },
                {
                    "id": 613,
                    "Guid": "53C0A71D-7506-47E4-A313-BCD382549FD7",
                    "slug": "dining-with-a-view",
                    "name": "Dining with a View"
                },
                {
                    "id": 631,
                    "Guid": "816B8E02-CFD5-4F3D-B5CA-748742B5674C",
                    "slug": "seating-with-a-view",
                    "name": "Seating with a View"
                },
                {
                    "id": 342,
                    "Guid": "DB78D8E7-B339-4C11-B4CE-2677E4028594",
                    "slug": "afternoon-tea",
                    "name": "Afternoon Tea"
                },
                {
                    "id": 605,
                    "Guid": "8D59C7F9-7B06-4228-8462-D636244E1481",
                    "slug": "award-winning",
                    "name": "Award-Winning"
                },
                {
                    "id": 602,
                    "Guid": "7F6B6306-44D5-42BA-8468-7CD433FFA0B8",
                    "slug": "adults-only",
                    "name": "Adults Only"
                },
                {
                    "id": 357,
                    "Guid": "F22A9895-71ED-435D-8B32-91384803DEF7",
                    "slug": "buffet",
                    "name": "Buffet"
                },
                {
                    "id": 607,
                    "Guid": "AA9315D5-DEBE-416B-9715-DA79B15A3665",
                    "slug": "brunch-3",
                    "name": "Brunch"
                },
                {
                    "id": 612,
                    "Guid": "51BABE9C-DAB4-450A-8819-9C8AF24DE02C",
                    "slug": "dine-around",
                    "name": "Dine Around"
                },
                {
                    "id": 634,
                    "Guid": "C7012D89-E744-4612-AF7E-BED90CB54F0B",
                    "slug": "take-away-1",
                    "name": "Take-Away"
                },
                {
                    "id": 626,
                    "Guid": "CE5B5D09-8DD3-43CF-ADFA-754588BAE254",
                    "slug": "poolside-dining",
                    "name": "Poolside Dining"
                },
                {
                    "id": 628,
                    "Guid": "CA75E925-E261-405B-AE81-86147ADE51FB",
                    "slug": "romantic-dinner",
                    "name": "Romantic Dinner"
                },
                {
                    "id": 604,
                    "Guid": "98A179BB-F450-402B-811D-10C79991E99B",
                    "slug": "all-inclusive",
                    "name": "All Inclusive"
                },
                {
                    "id": 606,
                    "Guid": "DE0F9A2A-849E-4751-92BD-860BE45F61CD",
                    "slug": "beachside-dining",
                    "name": "Beachside Dining"
                },
                {
                    "id": 619,
                    "Guid": "AE4C23CF-8DE9-480D-823B-27E6BB3015A7",
                    "slug": "live-cooking-1",
                    "name": "Live Cooking"
                },
                {
                    "id": 620,
                    "Guid": "A379561A-46FF-428C-A5D7-6DF5FFA038EF",
                    "slug": "live-dj-1",
                    "name": "Live DJ"
                },
                {
                    "id": 623,
                    "Guid": "3CE5F97B-4935-46CC-BDF3-DA3F074FEBA7",
                    "slug": "music-bar",
                    "name": "Music Bar"
                },
                {
                    "id": 625,
                    "Guid": "F5F534E7-2C22-4236-B66C-A09B26ECDC05",
                    "slug": "pet-friendly",
                    "name": "Pet-Friendly"
                },
                {
                    "id": 629,
                    "Guid": "17117589-7FA5-48A5-AB3A-07CD2F5431FE",
                    "slug": "rooftop-bar-1",
                    "name": "Rooftop Bar"
                },
                {
                    "id": 632,
                    "Guid": "30AE0F4C-D737-4ECF-9C66-7F1F181FD8A5",
                    "slug": "speakeasy",
                    "name": "Speakeasy"
                }
            ]
        },
        {
            "name": "Meal Periods",
            "slug": "mealperiod",
            "taxonomies": [
                {
                    "id": 368,
                    "Guid": "525FC276-5C39-417E-AE20-64111D5D13DF",
                    "slug": "dinner",
                    "name": "Dinner"
                },
                {
                    "id": 366,
                    "Guid": "9F8C3ABC-B000-49EA-8B08-A87A5F3A339D",
                    "slug": "lunch",
                    "name": "Lunch"
                },
                {
                    "id": 365,
                    "Guid": "F938A9E1-EBE7-4F58-B675-626575AD2930",
                    "slug": "breakfast",
                    "name": "Breakfast"
                },
                {
                    "id": 563,
                    "Guid": "04B516DC-C01A-4D98-A1AE-34DC13C20B42",
                    "slug": "brunch-1",
                    "name": "Brunch"
                },
                {
                    "id": 367,
                    "Guid": "04CBEABE-A48D-4141-A81A-F87E47C3EB2A",
                    "slug": "afternoon-tea-1",
                    "name": "Afternoon Tea"
                },
                {
                    "id": 562,
                    "Guid": "3E7068FB-0F24-4711-AB09-F45094204ECE",
                    "slug": "nightcap",
                    "name": "Nightcap"
                },
                {
                    "id": 564,
                    "Guid": "B997A11D-A787-4534-A56E-9ECB6B109EF3",
                    "slug": "all-day-dining-1",
                    "name": "All-Day Dining"
                }
            ]
        },
        {
            "name": "Dress Codes",
            "slug": "attire",
            "taxonomies": [
                {
                    "id": 373,
                    "Guid": "80FE6576-3E3D-4F7E-99EA-8961A9A4E241",
                    "slug": "casual-1",
                    "name": "Casual"
                },
                {
                    "id": 379,
                    "Guid": "3BA52000-36A3-43E7-A6B0-AB665532D3B1",
                    "slug": "smart-casual-1",
                    "name": "Smart Casual"
                },
                {
                    "id": 371,
                    "Guid": "82F5C08C-982C-4765-B116-7C8E29E6EC2F",
                    "slug": "beachwear",
                    "name": "Beachwear"
                },
                {
                    "id": 376,
                    "Guid": "D8F871F3-8046-4EFB-89C5-AA0339FD59BE",
                    "slug": "formal",
                    "name": "Formal"
                }
            ]
        },
        {
            "name": "Neighbourhoods",
            "slug": "location",
            "taxonomies": [
                {
                    "city": "Dubai",
                    "locations": [
                        {
                            "id": 464,
                            "Guid": "34AB6458-302B-4236-88C8-3E40BE305FC0",
                            "slug": "al-barsha",
                            "name": "Al Barsha"
                        },
                        {
                            "id": 419,
                            "Guid": "D090D271-9BEC-482D-832F-25BEA5088FA8",
                            "slug": "al-jaddaf",
                            "name": "Al Jaddaf"
                        },
                        {
                            "id": 420,
                            "Guid": "9A5D9108-DF3D-47B0-B198-795456BF578F",
                            "slug": "bur-dubai",
                            "name": "Bur Dubai"
                        },
                        {
                            "id": 422,
                            "Guid": "93DA22C0-B39E-4FE9-A4AA-6DCC49A58CA9",
                            "slug": "business-bay",
                            "name": "Business Bay"
                        },
                        {
                            "id": 424,
                            "Guid": "BAD3F322-D944-446E-B942-453B43E5651D",
                            "slug": "city-walk",
                            "name": "City Walk"
                        },
                        {
                            "id": 418,
                            "Guid": "D6FF0C78-53F1-458E-84BD-D6FE41478903",
                            "slug": "deira",
                            "name": "Deira"
                        },
                        {
                            "id": 421,
                            "Guid": "C10C7A42-39BA-40BB-AC70-8501EC85EB3D",
                            "slug": "difc",
                            "name": "DIFC"
                        },
                        {
                            "id": 466,
                            "Guid": "780CB884-6E3D-4493-8C87-2D98291DA6E8",
                            "slug": "downtown-dubai",
                            "name": "Downtown Dubai"
                        },
                        {
                            "id": 417,
                            "Guid": "3A53BBF0-5346-4D49-9577-93D36AF5B516",
                            "slug": "dubai-marina",
                            "name": "Dubai Marina"
                        },
                        {
                            "id": 444,
                            "Guid": "2AA42807-BF94-4D13-A48A-525D9592BB5B",
                            "slug": "dubai-production-city",
                            "name": "Dubai Production City"
                        },
                        {
                            "id": 445,
                            "Guid": "F63EB76C-5C25-43F1-84C6-775339D66FD5",
                            "slug": "garhood",
                            "name": "Garhood"
                        },
                        {
                            "id": 427,
                            "Guid": "01D2BCAD-4036-4B97-83AF-BCCB856C2361",
                            "slug": "jbr",
                            "name": "JBR"
                        },
                        {
                            "id": 425,
                            "Guid": "40BC7F17-3ED7-4CD8-A80D-2E0EB4C09A26",
                            "slug": "jumeirah",
                            "name": "Jumeirah"
                        },
                        {
                            "id": 428,
                            "Guid": "53053B70-DD4C-409C-A85D-E8144ECC1159",
                            "slug": "the-palm",
                            "name": "The Palm"
                        }
                    ]
                }
            ]
        },
        {
            "name": "Sort by",
            "slug": "sort",
            "taxonomies": [
                {
                    "name": "Nearest to Me",
                    "slug": "nearest_to_me"
                },
                {
                    "name": "Trending this Week",
                    "slug": "trending"
                },
                {
                    "name": "Newest Added",
                    "slug": "newest_first"
                },
                {
                    "name": "Alphabetical",
                    "slug": "title_a_z"
                }
            ]
        },
        {
            "name": "Price Ranges",
            "slug": "pricerange",
            "taxonomies": [
                {
                    "name": "Low",
                    "slug": "low"
                },
                {
                    "name": "Medium",
                    "slug": "medium"
                },
                {
                    "name": "High",
                    "slug": "high"
                }
            ]
        }
    ],
    "statusCode": 200,
    "message": "success"
}
    """



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimesWorldFilterTheme {
        Greeting("Android")
    }
}