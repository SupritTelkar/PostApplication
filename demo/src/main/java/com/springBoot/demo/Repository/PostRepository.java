package com.springBoot.demo.Repository;

import com.springBoot.demo.Entity.PostData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostData,Long> {

}
