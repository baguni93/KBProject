<template>
  <div>
    <div class="card">
      <FeedMoreButton v-if="feed.userId === userId" @click="openMenu" />
      <CardProfile
        :user-id="feed.userId"
        :profile-image-name="feed.sender ? feed.sender.profileImageName : null"
        :nickname="feed.sender ? feed.sender.nickname : (feed.userName || '회원')"
        :created-at="feed.createdAt"
        :visibility="feed.visibility"
        :show-visibility="true"
      />
      <!-- <FeedTypeTags :feed="feed" /> -->
      <div class="content">
        {{ feed.content }}
      </div>

      <FeedBody :feed="feed" />

      <FeedFooter
        :like-count="feed.likeCount"
        :comment-count="feed.commentCount"
        :liked="feed.liked"
        @like="handleLike"
        @comment="openComment"
      />
      <CommentBottomSheet v-model="showComment" :feed-id="feed.feedId" />
    </div>
    <FeedBottomSheet v-model="showMenu" @edit="onEdit" @delete="onDelete" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import CardProfile from '../common/CardProfile.vue';
import FeedBottomSheet from './FeedBottomSheet.vue';
import FeedFooter from './FeedFooter.vue';
import FeedBody from './body/FeedBody.vue';
import FeedTypeTags from './FeedTypeTags.vue';
import FeedMoreButton from './FeedMoreButton.vue';
import CommentBottomSheet from './CommentBottomSheet.vue';
import { useFeedStore } from '@/stores/feed';
import { useRouter } from 'vue-router';

//test user Id
import { useUserStore } from '@/stores/user';
const userStore = useUserStore();
const userId = userStore.userId;

const router = useRouter();
const feedStore = useFeedStore();

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const feedId = computed(() => props.feed.feedId);

const handleLike = async () => {
  try {
    await feedStore.toggleLike({
      feedId: feedId.value,
      userId,
    });
  } catch (e) {
    console.log(e);
  }
};

const showComment = ref(false);
const showMenu = ref(false);

const openMenu = () => {
  showMenu.value = true;
};
const openComment = async () => {
  showComment.value = true;
};

//토스트 팝업
const onEdit = () => {
  console.log('수정');
  router.push(`/feed/edit/${feedId.value}`);
};

const onDelete = async () => {
  try {
    await feedStore.deleteFeed(feedId.value);
  } catch (e) {
    console.log(e);
  }
};
</script>

<style scoped>
.card {
  width: 100%;

  padding: 20px;

  border-radius: 13px;

  background: white;

  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);

  position: relative;
}

.content {
  margin: 5px 5px 5px 5px;

  font-size: 13px;
  font-weight: bold;
}
</style>
