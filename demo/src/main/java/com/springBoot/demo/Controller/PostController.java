package com.springBoot.demo.Controller;

import com.springBoot.demo.DTO.Post;
import com.springBoot.demo.Service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post){
        return new ResponseEntity<>(postService.createPost(post), HttpStatus.CREATED);
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<Post>> createAndGetAllPosts(@RequestBody List<Post> posts) {
        List<Post> allPosts = postService.createAndGetAllPosts(posts);
        return ResponseEntity.ok(allPosts);
    }
    @GetMapping
    public List<Post> getAllPost(){
      return   postService.getAllposts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable(name = "id") long id){
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updateData(@RequestBody Post post ,@PathVariable(name="id")long id){
            Post postResp =postService.updatePostData(post,id);
            return new ResponseEntity<>(postResp,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable(name = "id") long id){
        postService.deletePostById(id);
        return new ResponseEntity<>("Data deleted successfully.", HttpStatus.OK);
    }
}
