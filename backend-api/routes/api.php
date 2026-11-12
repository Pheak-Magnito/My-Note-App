<?php

use App\Http\Controllers\Api\CategoryController;
use App\Http\Controllers\Api\NoteController;
use Illuminate\Support\Facades\Route;

Route::apiResource('categories', CategoryController::class);
Route::apiResource('notes', NoteController::class);
