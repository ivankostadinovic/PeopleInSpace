package com.surrus.peopleinspace.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import androidx.ui.tooling.preview.Preview
import androidx.ui.tooling.preview.PreviewParameter
import com.surrus.common.remote.Assignment
import com.surrus.common.remote.IssPosition
import com.surrus.common.repository.getLogger
import dev.chrisbanes.accompanist.coil.CoilImage
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {
    private val peopleInSpaceViewModel: PeopleInSpaceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainLayout(peopleInSpaceViewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        getLogger().d("MainActivity", "onStart")
    }

    override fun onStop() {
        getLogger().d("MainActivity", "onStop")
        super.onStop()
    }
}


sealed class Screen(val title: String) {
    object PersonListScreen : Screen("PersonList")
    object PersonDetailsDetails : Screen("PersonDetails")
}

@Composable
fun MainLayout(peopleInSpaceViewModel: PeopleInSpaceViewModel) {
    val navController = rememberNavController()

    PeopleInSpaceTheme {
        NavHost(navController, startDestination = Screen.PersonListScreen.title) {
            composable(Screen.PersonListScreen.title) {
                PersonList(peopleInSpaceViewModel = peopleInSpaceViewModel,
                    personSelected = {
                        navController.navigate(Screen.PersonDetailsDetails.title + "/${it.name}")
                    }
                )
            }
            composable(Screen.PersonDetailsDetails.title + "/{person}") { backStackEntry ->
                PersonDetailsView(peopleInSpaceViewModel,
                    backStackEntry.arguments?.get("person") as String,
                    popBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun PersonList(peopleInSpaceViewModel: PeopleInSpaceViewModel, personSelected : (person : Assignment) -> Unit) {
    val peopleState = peopleInSpaceViewModel.peopleInSpace.collectAsState()

    val issPosition = peopleInSpaceViewModel.issPosition.observeAsState(IssPosition(0.0, 0.0))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("People In Space") })
        },
        bodyContent = {
            Column {
                ISSPosition(issPosition.value)
                Divider(thickness = 2.dp)
                LazyColumnFor(items = peopleState.value, itemContent = { person ->
                    val personImageUrl = peopleInSpaceViewModel.getPersonImage(person.name)
                    PersonView(personImageUrl, person, personSelected)
                })
            }
        }
    )
}

@Composable
fun ISSPosition(issPosition: IssPosition) {
    Text(text = "ISS Position = (${issPosition.latitude}, ${issPosition.longitude})",
        Modifier.padding(16.dp) + Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = typography.h6)

}



@Composable
fun PersonView(personImageUrl: String, person: Assignment, personSelected : (person : Assignment) -> Unit) {
    Row(
        modifier =  Modifier.fillMaxWidth() + Modifier.clickable(onClick = { personSelected(person) })
                + Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
    ) {

        if (personImageUrl.isNotEmpty()) {
            CoilImage(data = personImageUrl, modifier = Modifier.preferredSize(60.dp))
        } else {
            Spacer(modifier = Modifier.preferredSize(60.dp))
        }

        Spacer(modifier = Modifier.preferredSize(12.dp))

        Column {
            Text(text = person.name, style = TextStyle(fontSize = 20.sp))
            Text(text = person.craft, style = TextStyle(color = Color.DarkGray, fontSize = 14.sp))
        }
    }
}

@Composable
fun PersonDetailsView(peopleInSpaceViewModel: PeopleInSpaceViewModel, personName: String, popBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(personName) },
                navigationIcon = {
                    IconButton(onClick = { popBack() }) {
                        Icon(Icons.Filled.ArrowBack)
                    }
                }
            )
        },
        bodyContent = {
            ScrollableColumn(modifier = Modifier.padding(16.dp) + Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val person = peopleInSpaceViewModel.getPerson(personName)
                person?.let {
                    Text(person.name, style = MaterialTheme.typography.h4)
                    Spacer(modifier = Modifier.preferredSize(12.dp))

                    val imageUrl = peopleInSpaceViewModel.getPersonImage(person.name)
                    if (imageUrl.isNotEmpty()) {
                        CoilImage(data = imageUrl, modifier = Modifier.preferredSize(240.dp))
                    }
                    Spacer(modifier = Modifier.preferredSize(24.dp))

                    val bio = peopleInSpaceViewModel.getPersonBio(person.name)
                    Text(bio, style = MaterialTheme.typography.body1)
                }
            }
        }
    )
}


@Preview
@Composable
fun DefaultPreview(@PreviewParameter(PersonProvider::class) person: Assignment) {
    MaterialTheme {
        PersonView("", person, personSelected = {})
    }
}