package com.example.cvtt.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

val  photos = Array(100){it}
@Composable
fun Album(modifier: Modifier = Modifier) {
    val gridState = rememberLazyGridState()
    var autoScrollSpeed by remember { mutableFloatStateOf(0f) }
    PhotoGrid(modifier = modifier, state = gridState,
        setAutoScrollSpeed = { autoScrollSpeed = it }
        )
    LaunchedEffect(autoScrollSpeed) {
        if (autoScrollSpeed != 0f) {
            while (isActive) {
                gridState.scrollBy(autoScrollSpeed)
                delay(10)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(modifier: Modifier = Modifier,state:LazyGridState,
setAutoScrollSpeed: (Float) -> Unit = { },
) {
    var selectedIds by rememberSaveable{mutableStateOf(emptySet<Int>())}
    val inSelectionMode by remember {derivedStateOf { selectedIds.isNotEmpty() }}
    LazyVerticalGrid(
        state = state,
        contentPadding = PaddingValues(horizontal = 10.dp),
        columns = GridCells.Adaptive(90.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier.photoGridDragHandler(
            lazyGridState = state,
            selectedIds = { selectedIds },
            setSelectedIds = { selectedIds = it },
            setAutoScrollSpeed = setAutoScrollSpeed,
            autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() }
        )
        ) {
        items(photos.size,key={it}){index->
            val selected by remember { derivedStateOf { selectedIds.contains(index) }}
            ImageListItem(index = index,selected=selected, inSelectionMode = inSelectionMode,
                modifier = if(inSelectionMode){
                    Modifier.toggleable(
                        value = selected,
                        onValueChange = {
                          if (it) selectedIds += index else selectedIds -= index
                        }
                    )
                }else{
                    Modifier.combinedClickable(
                        onClick = {  },
                        onLongClick = { selectedIds += index }
                    )
                }
            )
        }
    }
}
@Composable
fun ImageListItem(modifier: Modifier = Modifier,index:Int,selected:Boolean,
                  inSelectionMode:Boolean
                  ) {
    val TAG = "ImageListItem"
    val transition = updateTransition(selected,label="selected")
    Log.d(TAG,"$selected")
    val padding by transition.animateDp(label = "padding") { selected ->
        if (selected) 10.dp else 0.dp
    }
    val roundedCornerShape by transition.animateDp(label = "corner") { selected ->
        if (selected) 16.dp else 0.dp
    }
    Surface(modifier = modifier.aspectRatio(1f) ){
        Box( contentAlignment = Alignment.Center, modifier = Modifier.padding(padding).clip(
            RoundedCornerShape(roundedCornerShape)).background(Color.Green)
        ){
            if(inSelectionMode){
                if (selected) {
                    val bgColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    Icon(
                        Icons.Filled.CheckCircle,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .border(2.dp, bgColor, CircleShape)
                            .clip(CircleShape)
                            .background(bgColor)
                    )
                }else{
                    Icon(
                        Icons.Filled.AddCircle,
                        tint = Color.White.copy(alpha = 0.7f),
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            Text("Item$index",color = Color.White)
        }
    }
}
fun Modifier.photoGridDragHandler(
    lazyGridState: LazyGridState,
    selectedIds:()->Set<Int>,
    autoScrollThreshold:Float,
    setSelectedIds:(Set<Int>)->Unit = {},
    setAutoScrollSpeed:(Float)->Unit = {},
)=this.pointerInput(autoScrollThreshold,setSelectedIds,setAutoScrollSpeed){
    fun photoIdAtOffset(hitPoint:Offset):Int? = lazyGridState.layoutInfo.visibleItemsInfo.find{itemInfo->
            itemInfo.size.toIntRect().contains(hitPoint.round() - itemInfo.offset)

        }?.key as? Int
    var initialPhotoId: Int? = null
    var currentPhotoId: Int? = null

    detectDragGesturesAfterLongPress(
        onDragStart = {offset ->
            Log.d("PhotoGrid","onDragStart")
            Log.d("PhotoGrid","onDragStart: $offset")

            photoIdAtOffset(offset)?.let{key->
                Log.d("PhotoGrid","onDragStart: key:$key")
//                if(!selectedIds().contains(key)){
                    Log.d("PhotoGrid","onDragStart: $key")
                    initialPhotoId = key
                    currentPhotoId = key
                    setSelectedIds(selectedIds()+key)
//                }
            }
        },
        onDragCancel = { setAutoScrollSpeed(0f); initialPhotoId = null },
        onDragEnd = { setAutoScrollSpeed(0f); initialPhotoId = null },
        onDrag = {change,_ ->
            Log.d("PhotoGrid","onDrag")
            Log.d("PhotoGrid","onDrag: initialPhotoId:${initialPhotoId}")
            if(initialPhotoId != null){
                Log.d("PhotoGrid","onDrag: ${change.position}")
               val distFromBottom = lazyGridState.layoutInfo.viewportSize.height -change.position.y
                val distFromTop = change.position.y
                setAutoScrollSpeed(
                    when {
                        distFromBottom < autoScrollThreshold -> autoScrollThreshold - distFromBottom
                        distFromTop < autoScrollThreshold -> -(autoScrollThreshold - distFromTop)
                        else -> 0f
                    }
                )

                photoIdAtOffset(change.position)?.let { pointerPhotoId ->
                    Log.d("PhotoGrid","onDrag: photo:$pointerPhotoId")
                    if (currentPhotoId != pointerPhotoId) {
                        Log.d("PhotoGrid","onDrag: current:$currentPhotoId")
                        setSelectedIds(
                            selectedIds().addOrRemoveUpTo(pointerPhotoId, currentPhotoId, initialPhotoId)
                        )
                        currentPhotoId = pointerPhotoId
                    }
                }

            }
        }

    )


}
private fun Set<Int>.addOrRemoveUpTo(
    pointerKey: Int?,
    previousPointerKey: Int?,
    initialKey: Int?
): Set<Int> {
    return if (pointerKey == null || previousPointerKey == null || initialKey == null) {
        this
    } else {
        this
            .minus(initialKey..previousPointerKey)
            .minus(previousPointerKey..initialKey)
            .plus(initialKey..pointerKey)
            .plus(pointerKey..initialKey)
    }
}
