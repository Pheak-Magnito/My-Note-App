<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\Note;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class NoteApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_all_notes_are_available_without_authentication(): void
    {
        $note = Note::factory()->create(['title' => 'My note']);
        Note::factory()->create(['title' => 'Another note']);

        $this->getJson('/api/notes')
            ->assertOk()
            ->assertJsonCount(2, 'data')
            ->assertJsonFragment([
                'id' => $note->id,
                'title' => 'My note',
            ]);
    }

    public function test_valid_payload_creates_note_and_returns_201(): void
    {
        $category = Category::factory()->create();

        $this->postJson('/api/notes', [
            'title' => 'Meeting notes',
            'content' => 'Discuss project milestones.',
            'category_id' => $category->id,
        ])
            ->assertCreated()
            ->assertJsonPath('data.title', 'Meeting notes')
            ->assertJsonPath('data.category.id', $category->id);

        $this->assertDatabaseHas('notes', [
            'category_id' => $category->id,
            'title' => 'Meeting notes',
        ]);
    }

    public function test_missing_title_returns_422_without_creating_a_note(): void
    {
        $this->postJson('/api/notes', ['content' => 'No title'])
            ->assertUnprocessable()
            ->assertJsonValidationErrors('title');

        $this->assertDatabaseCount('notes', 0);
    }

    public function test_note_can_be_updated_and_deleted_without_authentication(): void
    {
        $note = Note::factory()->create(['title' => 'Old title']);

        $this->patchJson('/api/notes/'.$note->id, ['title' => 'Updated title'])
            ->assertOk()
            ->assertJsonPath('data.title', 'Updated title');

        $this->assertDatabaseHas('notes', [
            'id' => $note->id,
            'title' => 'Updated title',
        ]);

        $this->deleteJson('/api/notes/'.$note->id)->assertNoContent();

        $this->assertDatabaseMissing('notes', ['id' => $note->id]);
    }
}
