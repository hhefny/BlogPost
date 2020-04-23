package com.hefny.hady.blogpost.persistence

import androidx.room.*
import com.hefny.hady.blogpost.models.BlogPost
import com.hefny.hady.blogpost.util.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface BlogPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long

    @Query(
        """
        UPDATE blog_post SET title = :title,
        body = :body,
        image = :image
        WHERE pk = :pk
    """
    )
    suspend fun updateBlogPost(
        pk: Int,
        title: String,
        body: String,
        image: String
    )

    @Delete
    suspend fun deleteBlogPost(blogPost: BlogPost)

    @Query(
        """
        SELECT * FROM blog_post
         WHERE title LIKE '%' || :query || '%' 
         OR body LIKE '%' || :query || '%'
         OR username LIKE '%' || :query || '&'
         LIMIT (:page * :pageSize)
    """
    )
    suspend fun searchBlogPosts(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query ||'%'
        ORDER BY date_updated DESC LIMIT(:page * :pageSize)
    """
    )
    suspend fun searchBlogPostsOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY date_updated ASC LIMIT(:page * :pageSize)
    """
    )
    suspend fun searchBlogPostsOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY username DESC LIMIT(:page * :pageSize)
    """
    )
    suspend fun searchBlogPostsOrderByAuthorDESC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>

    @Query(
        """
        SELECT * FROM blog_post
        WHERE title LIKE '%' || :query || '%'
        OR body LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY username ASC LIMIT(:page * :pageSize)
    """
    )
    suspend fun searchBlogPostsOrderByAuthorASC(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): List<BlogPost>
}