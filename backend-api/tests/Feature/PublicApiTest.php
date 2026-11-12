<?php

namespace Tests\Feature;

use Tests\TestCase;

class PublicApiTest extends TestCase
{
    public function test_authentication_routes_are_not_available(): void
    {
        $this->postJson('/register')->assertNotFound();
        $this->postJson('/login')->assertNotFound();
        $this->postJson('/forgot-password')->assertNotFound();
        $this->postJson('/reset-password')->assertNotFound();
        $this->postJson('/logout')->assertNotFound();
        $this->getJson('/api/user')->assertNotFound();
    }
}
