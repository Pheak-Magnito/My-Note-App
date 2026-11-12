<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Http\Resources\NoteResource;
use App\Models\Note;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\AnonymousResourceCollection;
use Illuminate\Http\Response;
use Illuminate\Validation\Rule;

class NoteController extends Controller
{
    public function index(): AnonymousResourceCollection
    {
        return NoteResource::collection(
            Note::query()->with('category')->latest()->get(),
        );
    }

    public function store(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'title' => ['required', 'string', 'max:255'],
            'content' => ['nullable', 'string'],
            'category_id' => ['nullable', Rule::exists('categories', 'id')],
        ]);

        $note = Note::query()->create($validated)->load('category');

        return (new NoteResource($note))->response()->setStatusCode(201);
    }

    public function show(string $note): NoteResource
    {
        return new NoteResource(Note::query()->with('category')->findOrFail($note));
    }

    public function update(Request $request, string $note): NoteResource
    {
        $note = Note::query()->findOrFail($note);

        $validated = $request->validate([
            'title' => ['sometimes', 'required', 'string', 'max:255'],
            'content' => ['sometimes', 'nullable', 'string'],
            'category_id' => ['sometimes', 'nullable', Rule::exists('categories', 'id')],
        ]);

        $note->update($validated);

        return new NoteResource($note->load('category'));
    }

    public function destroy(string $note): Response
    {
        Note::query()->findOrFail($note)->delete();

        return response()->noContent();
    }
}
