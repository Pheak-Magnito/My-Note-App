<?php

namespace Tests\Feature;

use App\Models\Category;
use App\Models\Note;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class CategoryApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_all_categories_are_available_without_authentication(): void
    {
        $category = Category::factory()->create(['name' => 'Work']);
        Category::factory()->create(['name' => 'Private']);

        $this->getJson('/api/categories')
            ->assertOk()
            ->assertJsonCount(2, 'data')
            ->assertJsonFragment([
                'id' => $category->id,
                'name' => 'Work',
            ]);
    }

    public function test_valid_payload_creates_category_and_returns_201(): void
    {
        $this->postJson('/api/categories', ['name' => 'Work'])
            ->assertCreated()
            ->assertJsonPath('data.name', 'Work');

        $this->assertDatabaseHas('categories', [
            'name' => 'Work',
        ]);
    }

    public function test_duplicate_category_name_returns_422_without_creating_another_category(): void
    {
        Category::factory()->create(['name' => 'Work']);

        $this->postJson('/api/categories', ['name' => 'Work'])
            ->assertUnprocessable()
            ->assertJsonValidationErrors('name');

        $this->assertDatabaseCount('categories', 1);
    }

    public function test_category_can_be_updated_without_authentication(): void
    {
        $category = Category::factory()->create(['name' => 'Old name']);

        $this->patchJson('/api/categories/'.$category->id, ['name' => 'New name'])
            ->assertOk()
            ->assertJsonPath('data.name', 'New name');

        $this->assertDatabaseHas('categories', [
            'id' => $category->id,
            'name' => 'New name',
        ]);
    }

    public function test_deleting_category_keeps_notes_and_clears_their_category(): void
    {
        $category = Category::factory()->create();
        $note = Note::factory()->create(['category_id' => $category->id]);

        $this->deleteJson('/api/categories/'.$category->id)
            ->assertNoContent();

        $this->assertModelExists($note->refresh());
        $this->assertNull($note->category_id);
    }
}
