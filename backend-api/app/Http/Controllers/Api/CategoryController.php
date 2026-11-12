<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\CategoryResource;
use App\Models\Category;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;
use Illuminate\Http\Response;
use Illuminate\Validation\Rule;

class CategoryController extends Controller
{
    public function index(): AnonymousResourceCollection
    {
        return CategoryResource::collection(Category::query()->latest()->get());
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'name' => [
                'required',
                'string',
                'max:255',
                Rule::unique('categories', 'name'),
            ],
        ]);

        $category = Category::query()->create($validated);

        return (new CategoryResource($category))->response()->setStatusCode(201);
    }

    public function show(string $category): CategoryResource
    {
        return new CategoryResource(Category::query()->findOrFail($category));
    }

    public function update(Request $request, string $category): CategoryResource
    {
        $category = Category::query()->findOrFail($category);

        $validated = $request->validate([
            'name' => [
                'sometimes',
                'required',
                'string',
                'max:255',
                Rule::unique('categories', 'name')
                    ->ignore($category->id),
            ],
        ]);

        $category->update($validated);

        return new CategoryResource($category);
    }

    public function destroy(string $category): Response
    {
        Category::query()->findOrFail($category)->delete();

        return response()->noContent();
    }
}
