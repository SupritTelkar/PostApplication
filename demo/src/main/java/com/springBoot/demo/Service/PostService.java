package com.springBoot.demo.Service;

import com.springBoot.demo.DTO.Post;
import com.springBoot.demo.Entity.PostData;

import java.util.List;

public interface PostService {
    Post createPost(Post postDto);
    List<Post> getAllposts();
    Post getPostById(long id);
    Post updatePostData(Post post, long id);
    void deletePostById(long id);
    List<Post> createAndGetAllPosts(List<Post> posts);

}
