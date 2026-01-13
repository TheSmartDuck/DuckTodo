import Vue from 'vue'
import VueRouter from 'vue-router'
import LoginView from '../views/LoginView.vue'
import IndexView from '../views/IndexView.vue'
import UserInformationView from '../views/UserInformationView.vue'
import TeamCreateView from '../views/TeamCreateView.vue'
import TeamDetailView from '../views/TeamDetailView.vue'
import TaskGroupCreatePrivateView from '../views/TaskGroupCreatePrivateView.vue'
import TaskGroupDetailView from '../views/TaskGroupDetailView.vue'
import RecentlyToDoView from '../views/RecentlyToDoView.vue'
import ScheduleOverviewView from '../views/ScheduleOverviewView.vue'
import DataPanelView from '../views/DataPanelView.vue'
import ToolsFactoryView from '../views/ToolsFactoryView.vue'
import HomeView from '../views/HomeView.vue'
import ThemeSampleView from '../views/ThemeSampleView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/themeSample',
    name: 'theme-sample',
    component: ThemeSampleView
  },
  {
    path: '/',
    component: IndexView,
    children: [
      {
        path: '',
        redirect: 'home'
      },
      {
        path: 'home',
        name: 'home',
        component: HomeView
      },
      {
        path: 'userinformation',
        name: 'userinformation',
        component: UserInformationView
      },
      {
        path: 'team/create',
        name: 'team-create',
        component: TeamCreateView
      },
      {
        path: 'taskgroup/create-private',
        name: 'taskgroup-create-private',
        component: TaskGroupCreatePrivateView
      },
      {
        path: 'taskgroup/:taskGroupId',
        name: 'taskgroup-detail',
        component: TaskGroupDetailView,
        props: true
      },
      {
        path: 'team/:teamId',
        name: 'team-detail',
        component: TeamDetailView,
        props: true
      },
      {
        path: 'recent',
        name: 'recent',
        component: RecentlyToDoView
      },
      {
        path: 'schedule',
        name: 'schedule',
        component: ScheduleOverviewView
      },
      {
        path: 'datapanel',
        name: 'datapanel',
        component: DataPanelView
      }
      ,{
        path: 'tools',
        name: 'tools',
        component: ToolsFactoryView
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  }
]

const router = new VueRouter({
  routes
})

// 全局前置守卫：无 token 仅允许访问登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/themeSample') {
    return next()
  }
  if (!token) {
    return next({ path: '/login' })
  }
  next()
})

export default router
