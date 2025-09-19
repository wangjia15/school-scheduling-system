export interface WebSocketMessage {
  type: string
  payload: any
  timestamp: string
  userId?: number
}

export interface ScheduleUpdateMessage {
  type: 'SCHEDULE_CREATED' | 'SCHEDULE_UPDATED' | 'SCHEDULE_DELETED' | 'SCHEDULE_CONFLICT'
  scheduleId: number
  semester: string
  academicYear: string
  teacherId?: number
  classroomId?: number
  courseId?: number
  conflict?: {
    id: number
    type: string
    severity: string
    description: string
  }
}

export interface TeacherUpdateMessage {
  type: 'TEACHER_CREATED' | 'TEACHER_UPDATED' | 'TEACHER_DELETED' | 'TEACHER_AVAILABILITY_CHANGED'
  teacherId: number
  employeeId: string
  name: string
}

export interface ClassroomUpdateMessage {
  type: 'CLASSROOM_CREATED' | 'CLASSROOM_UPDATED' | 'CLASSROOM_DELETED' | 'CLASSROOM_MAINTENANCE'
  classroomId: number
  roomNumber: string
  building: string
}

export interface SystemNotificationMessage {
  type: 'SYSTEM_ALERT' | 'MAINTENANCE_SCHEDULED' | 'BACKUP_COMPLETED' | 'PERFORMANCE_WARNING'
  title: string
  message: string
  severity: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS'
  actionUrl?: string
}

export interface ConflictDetectedMessage {
  id: number
  conflictType: string
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  description: string
  resolutionStatus: string
  detectedAt: string
  requiresImmediateAttention: boolean
  hoursSinceDetection: number
  conflictSummary: string
  entityType?: string
  entityId?: number
}

export interface ConflictResolvedMessage {
  conflictId: number
  resolutionNotes: string
  resolvedAt: string
}

export interface ConflictStatsMessage {
  totalConflicts: number
  pendingConflicts: number
  criticalConflicts: number
  highSeverityConflicts: number
  mediumSeverityConflicts: number
  lowSeverityConflicts: number
  lastUpdated: string
}

export interface ConflictSystemAlertMessage {
  level: 'info' | 'warning' | 'error' | 'critical'
  title: string
  message: string
  entityType?: string
  entityId?: number
  timestamp: string
}

export interface UserActivityMessage {
  type: 'USER_LOGIN' | 'USER_LOGOUT' | 'SCHEDULE_GENERATION_STARTED' | 'SCHEDULE_GENERATION_COMPLETED'
  userId: number
  username: string
  action: string
  details?: any
}

type MessageHandler = (message: WebSocketMessage) => void

class WebSocketService {
  private ws: WebSocket | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 1000
  private isConnected = false
  private messageHandlers: Map<string, MessageHandler[]> = new Map()
  private reconnectTimer: NodeJS.Timeout | null = null
  private heartbeatTimer: NodeJS.Timeout | null = null

  constructor() {
    // Initialize connection when service is created
    this.connect()
  }

  connect() {
    try {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = import.meta.env.VITE_WS_HOST || window.location.host
      const wsUrl = `${protocol}//${host}/ws/conflicts`

      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = this.handleOpen.bind(this)
      this.ws.onmessage = this.handleMessage.bind(this)
      this.ws.onclose = this.handleClose.bind(this)
      this.ws.onerror = this.handleError.bind(this)

    } catch (error) {
      console.error('WebSocket connection error:', error)
      this.scheduleReconnect()
    }
  }

  private handleOpen() {
    console.log('WebSocket connected')
    this.isConnected = true
    this.reconnectAttempts = 0

    // Start heartbeat
    this.startHeartbeat()

    // Send authentication message
    this.authenticate()
  }

  private handleMessage(event: MessageEvent) {
    try {
      const message: WebSocketMessage = JSON.parse(event.data)

      // Reset heartbeat timer
      this.resetHeartbeat()

      // Route message to appropriate handlers
      const handlers = this.messageHandlers.get(message.type) || []
      handlers.forEach(handler => handler(message))

      // Log message for debugging
      if (import.meta.env.DEV) {
        console.log('WebSocket message received:', message)
      }

    } catch (error) {
      console.error('Error parsing WebSocket message:', error)
    }
  }

  private handleClose(event: CloseEvent) {
    console.log('WebSocket disconnected:', event.code, event.reason)
    this.isConnected = false
    this.cleanup()

    if (event.code !== 1000) { // 1000 = normal closure
      this.scheduleReconnect()
    }
  }

  private handleError(error: Event) {
    console.error('WebSocket error:', error)
    this.isConnected = false
    this.cleanup()
    this.scheduleReconnect()
  }

  private authenticate() {
    const token = localStorage.getItem('token')
    if (token) {
      this.send({
        type: 'AUTHENTICATE',
        payload: { token }
      })
    }
  }

  private startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected) {
        this.send({
          type: 'HEARTBEAT',
          payload: { timestamp: Date.now() }
        })
      }
    }, 30000) // Send heartbeat every 30 seconds
  }

  private resetHeartbeat() {
    if (this.heartbeatTimer) {
      clearTimeout(this.heartbeatTimer as any)
      this.startHeartbeat()
    }
  }

  private cleanup() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  private scheduleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts)

      console.log(`Scheduling reconnect attempt ${this.reconnectAttempts + 1} in ${delay}ms`)

      this.reconnectTimer = setTimeout(() => {
        this.reconnectAttempts++
        this.connect()
      }, delay)
    } else {
      console.error('Max reconnection attempts reached')
    }
  }

  send(message: Omit<WebSocketMessage, 'timestamp'>) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const fullMessage: WebSocketMessage = {
        ...message,
        timestamp: new Date().toISOString()
      }
      this.ws.send(JSON.stringify(fullMessage))
    } else {
      console.warn('WebSocket not connected, cannot send message')
    }
  }

  subscribe(messageType: string, handler: MessageHandler) {
    if (!this.messageHandlers.has(messageType)) {
      this.messageHandlers.set(messageType, [])
    }

    this.messageHandlers.get(messageType)!.push(handler)

    // Return unsubscribe function
    return () => {
      const handlers = this.messageHandlers.get(messageType)
      if (handlers) {
        const index = handlers.indexOf(handler)
        if (index > -1) {
          handlers.splice(index, 1)
        }
      }
    }
  }

  unsubscribe(messageType: string, handler: MessageHandler) {
    const handlers = this.messageHandlers.get(messageType)
    if (handlers) {
      const index = handlers.indexOf(handler)
      if (index > -1) {
        handlers.splice(index, 1)
      }
    }
  }

  subscribeToScheduleUpdates(handler: (message: ScheduleUpdateMessage) => void) {
    return this.subscribe('SCHEDULE_UPDATE', (message) => {
      handler(message.payload as ScheduleUpdateMessage)
    })
  }

  subscribeToTeacherUpdates(handler: (message: TeacherUpdateMessage) => void) {
    return this.subscribe('TEACHER_UPDATE', (message) => {
      handler(message.payload as TeacherUpdateMessage)
    })
  }

  subscribeToClassroomUpdates(handler: (message: ClassroomUpdateMessage) => void) {
    return this.subscribe('CLASSROOM_UPDATE', (message) => {
      handler(message.payload as ClassroomUpdateMessage)
    })
  }

  subscribeToSystemNotifications(handler: (message: SystemNotificationMessage) => void) {
    return this.subscribe('SYSTEM_NOTIFICATION', (message) => {
      handler(message.payload as SystemNotificationMessage)
    })
  }

  subscribeToUserActivity(handler: (message: UserActivityMessage) => void) {
    return this.subscribe('USER_ACTIVITY', (message) => {
      handler(message.payload as UserActivityMessage)
    })
  }

  subscribeToConflictDetected(handler: (message: ConflictDetectedMessage) => void) {
    return this.subscribe('conflict_detected', (message) => {
      handler(message.payload as ConflictDetectedMessage)
    })
  }

  subscribeToConflictResolved(handler: (message: ConflictResolvedMessage) => void) {
    return this.subscribe('conflict_resolved', (message) => {
      handler(message.payload as ConflictResolvedMessage)
    })
  }

  subscribeToConflictStats(handler: (message: ConflictStatsMessage) => void) {
    return this.subscribe('conflict_stats', (message) => {
      handler(message.payload as ConflictStatsMessage)
    })
  }

  subscribeToConflictAlerts(handler: (message: ConflictSystemAlertMessage) => void) {
    return this.subscribe('system_alert', (message) => {
      handler(message.payload as ConflictSystemAlertMessage)
    })
  }

  requestScheduleUpdates(semester: string, academicYear: string) {
    this.send({
      type: 'SUBSCRIBE_SCHEDULE_UPDATES',
      payload: { semester, academicYear }
    })
  }

  requestConflictUpdates() {
    this.send({
      type: 'SUBSCRIBE_CONFLICT_UPDATES',
      payload: {}
    })
  }

  requestTeacherUpdates() {
    this.send({
      type: 'SUBSCRIBE_TEACHER_UPDATES',
      payload: {}
    })
  }

  requestClassroomUpdates() {
    this.send({
      type: 'SUBSCRIBE_CLASSROOM_UPDATES',
      payload: {}
    })
  }

  disconnect() {
    if (this.ws) {
      this.ws.close(1000, 'Disconnect requested')
      this.ws = null
    }
    this.cleanup()
  }

  get connected() {
    return this.isConnected
  }
}

// Create singleton instance
export const websocketService = new WebSocketService()

// Auto-connect when the service is imported
if (typeof window !== 'undefined') {
  // Reconnect on page visibility change
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible' && !websocketService.connected) {
      websocketService.connect()
    }
  })
}

export type {
  WebSocketMessage,
  ScheduleUpdateMessage,
  TeacherUpdateMessage,
  ClassroomUpdateMessage,
  SystemNotificationMessage,
  ConflictDetectedMessage,
  ConflictResolvedMessage,
  ConflictStatsMessage,
  ConflictSystemAlertMessage,
  UserActivityMessage
}