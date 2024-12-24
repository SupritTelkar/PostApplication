package com.springBoot.demo.Service.Impl;

import com.springBoot.demo.DTO.Post;
import com.springBoot.demo.Entity.PostData;
import com.springBoot.demo.Exception.ResourcesNotFoundExcp;
import com.springBoot.demo.Repository.PostRepository;
import com.springBoot.demo.Service.PostService;
import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class PostServiceImple implements PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostServiceImple.class);
    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_KEY = "post:";
    private final ExecutorService executorService;
    private final Lock cacheLock;

    public PostServiceImple(PostRepository postRepository, RedisTemplate<String, Object> redisTemplate) {
        this.postRepository = postRepository;
        this.redisTemplate = redisTemplate;
        this.executorService = Executors.newFixedThreadPool(10);
        this.cacheLock = new ReentrantLock();
    }

    @Override
    @Transactional
    public Post createPost(Post postDto) {
        PostData postData = PostData.builder()
                .title(postDto.getTitle())
                .description(postDto.getDescription())
                .content(postDto.getContent())
                .build();

        PostData savedData = postRepository.save(postData);
        Post post = mapEntityDataToDto(savedData);

        executorService.submit(() -> {
            cacheLock.lock();
            try {
                redisTemplate.opsForValue().set(CACHE_KEY + post.getId(), post, 10, TimeUnit.MINUTES);
                logger.info("Post cached with key: {}", CACHE_KEY + post.getId());
            } finally {
                cacheLock.unlock();
            }
        });

        return post;
    }

    @Override
    public List<Post> createAndGetAllPosts(List<Post> posts) {
        List<PostData> postData = posts.stream()
                .map(m ->PostData.builder()
                        .title(m.getTitle())
                        .content(m.getDescription())
                        .description(m.getDescription())
                        .build())
                .collect(Collectors.toList());
     List<PostData> data =  postRepository.saveAll(postData);
     List<Post> postNewData =  data.stream()
             .map(this::mapEntityDataToDto)
             .collect(Collectors.toList());
     postNewData.forEach(post -> executorService.submit(() -> {
            String cacheKey = CACHE_KEY + post.getId();
            cacheLock.lock();
            try {
                redisTemplate.opsForValue().set(cacheKey, post, 10, TimeUnit.MINUTES);
                logger.info("Post cached with key: {}", cacheKey);
            } finally {
                cacheLock.unlock();
            }
        }));

        return postNewData;
    }

    @Override
    public List<Post> getAllposts() {
        String cacheKey = CACHE_KEY + "all";

        List<Post> cachedPosts = (List<Post>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPosts != null && !cachedPosts.isEmpty()) {
            logger.info("Retrieved all posts from cache with key: {}", cacheKey);
            return cachedPosts;
        }
        List<PostData> allData = postRepository.findAll();
        List<Post> allPosts = allData.stream()
                .map(this::mapEntityDataToDto)
                .collect(Collectors.toList());

        executorService.submit(() -> {
            cacheLock.lock();
            try {
                redisTemplate.opsForValue().set(cacheKey, allPosts, 10, TimeUnit.MINUTES);
                logger.info("All posts cached with key: {}", cacheKey);
            } finally {
                cacheLock.unlock();
            }
        });

        return allPosts;
    }


    @Override
    public Post getPostById(long id) {
        String redisKey = CACHE_KEY + id;

        Post cachedPost = null;
        cacheLock.lock();
        try {
            cachedPost = (Post) redisTemplate.opsForValue().get(redisKey);
        } finally {
            cacheLock.unlock();
        }

        if (cachedPost != null) {
            logger.info("Cache hit for key: {}", redisKey);
            return cachedPost;
        }

        PostData data = postRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundExcp("Post", "id", id));

        Post post = mapEntityDataToDto(data);

        executorService.submit(() -> {
            cacheLock.lock();
            try {
                redisTemplate.opsForValue().set(redisKey, post, 10, TimeUnit.MINUTES);
                logger.info("Post cached with key: {}", redisKey);
            } finally {
                cacheLock.unlock();
            }
        });

        return post;
    }

    @Override
    @Transactional
    public Post updatePostData(Post post, long id) {
        PostData data = postRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundExcp("Post", "id", id));

        data.setTitle(post.getTitle());
        data.setContent(post.getContent());
        data.setDescription(post.getDescription());

        PostData updatedData = postRepository.save(data);
        Post updatedPost = mapEntityDataToDto(updatedData);

        executorService.submit(() -> {
            cacheLock.lock();
            try {
                redisTemplate.opsForValue().set(CACHE_KEY + id, updatedPost, 10, TimeUnit.MINUTES);
                logger.info("Post updated and cached with key: {}", CACHE_KEY + id);
            } finally {
                cacheLock.unlock();
            }
        });

        return updatedPost;
    }

    @Override
    @Transactional
    public void deletePostById(long id) {
        PostData data = postRepository.findById(id)
                .orElseThrow(() -> new ResourcesNotFoundExcp("Post", "id", id));

        postRepository.delete(data);

        executorService.submit(() -> {
            cacheLock.lock();
            try {
                redisTemplate.delete(CACHE_KEY + id);
                logger.info("Post deleted from cache with key: {}", CACHE_KEY + id);
            } finally {
                cacheLock.unlock();
            }
        });
    }


    private Post mapEntityDataToDto(PostData data) {
        return Post.builder()
                .id(data.getId())
                .title(data.getTitle())
                .content(data.getContent())
                .description(data.getDescription())
                .build();
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

