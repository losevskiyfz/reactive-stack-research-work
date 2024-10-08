package com.losevskiyfz.reactivestackresearchwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.losevskiyfz.reactivestackresearchwork.domain.Book;
import com.losevskiyfz.reactivestackresearchwork.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Optional;

import static com.losevskiyfz.reactivestackresearchwork.mock.generator.MockBockGenerator.generateFakeBook;
import static com.losevskiyfz.reactivestackresearchwork.mock.generator.MockBockGenerator.generateFakeBooks;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BookControllerIT {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    BookRepository bookRepository;
    final ObjectMapper objectMapper = new ObjectMapper();
    final ObjectWriter objectWriter = objectMapper.writer();

    @Test
    void save() throws Exception {
        Book testBook = generateFakeBook();
        String requestJson = objectWriter.writeValueAsString(testBook);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        mockMvc.perform(
                        post("/api/v1/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(content().json(requestJson))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", "http://localhost/api/v1/book/" + testBook.id()))
                .andExpect(status().isCreated());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void get() throws Exception {
        int numberOfBooks = 41;
        int pageNumber = 1;
        int pageSize = 20;
        List<Book> testBooks = generateFakeBooks(numberOfBooks);
        Page<Book> responsePage = new PageImpl<>(testBooks, PageRequest.of(pageNumber, pageSize), numberOfBooks);
        when(bookRepository.getByTextPattern(any(PageRequest.class), any(String.class))).thenReturn(responsePage);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/book")
                )
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size", is(pageSize)))
                .andExpect(jsonPath("$.page.number", is(pageNumber)))
                .andExpect(jsonPath("$.page.totalElements", is(numberOfBooks)))
                .andExpect(jsonPath("$.page.totalPages", is(numberOfBooks / pageSize + 1)));
        verify(bookRepository).getByTextPattern(any(PageRequest.class), any(String.class));
    }

    @Test
    void updateIfDoesNotExist() throws Exception {
        String id = "99999999";
        Book testBook = generateFakeBook();
        String requestJson = objectWriter.writeValueAsString(testBook);
        when(bookRepository.findById(anyString())).thenReturn(Optional.empty());
        mockMvc.perform(
                put("/api/v1/book/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
        )
                .andExpect(status().isNotFound());
        verify(bookRepository).findById(anyString());
    }

    @Test
    void updateIfDoExist() throws Exception {
        String id = "5";
        Book testBook = generateFakeBook();
        String requestJson = objectWriter.writeValueAsString(testBook);
        when(bookRepository.findById(anyString())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        mockMvc.perform(
                        put("/api/v1/book/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNoContent());
        verify(bookRepository).findById(anyString());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void delete() throws Exception {
        String idToDelete = "99";
        doNothing().when(bookRepository).deleteById(anyString());
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/book/{id}", idToDelete)
                )
                .andExpect(status().isNoContent());
        verify(bookRepository).deleteById(anyString());
    }

}